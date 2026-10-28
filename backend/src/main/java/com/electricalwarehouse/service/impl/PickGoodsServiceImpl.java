package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.PickGoodsRequest;
import com.electricalwarehouse.dto.response.PickSuggestionResponse;
import com.electricalwarehouse.dto.response.StockOutResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.SerialStatus;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.InventoryRepository;
import com.electricalwarehouse.repository.ProductSerialRepository;
import com.electricalwarehouse.repository.StockOutReceiptRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.PickGoodsService;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import com.electricalwarehouse.service.approval.ApprovalService;
import com.electricalwarehouse.util.FifoFefoCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PickGoodsServiceImpl implements PickGoodsService {

    private final StockOutReceiptRepository receiptRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductSerialRepository serialRepository;
    private final UserRepository userRepository;
    private final FifoFefoCalculator calculator;
    private final ApprovalService approvalService;
    private final ApprovalPolicy approvalPolicy;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PickSuggestionResponse> getPickList(Long receiptId) {
        StockOutReceipt receipt = findPickable(receiptId);
        List<PickSuggestionResponse> result = new ArrayList<>();
        for (StockOutReceiptDetail d : receipt.getDetails()) {
            Product p = d.getProduct();
            int remaining = d.getQuantity() - d.getPickedQuantity();
            List<Inventory> rows = calculator.sortForPicking(
                    inventoryRepository.findByWarehouseIdAndProductId(receipt.getWarehouse().getId(), p.getId()),
                    p.isExpiryManaged());
            List<PickSuggestionResponse.PickLocation> locations = new ArrayList<>();
            int toSuggest = remaining;
            for (Inventory row : rows) {
                if (row.getQuantity() <= 0) {
                    continue;
                }
                int suggested = Math.min(toSuggest, row.getQuantity());
                toSuggest -= suggested;
                locations.add(new PickSuggestionResponse.PickLocation(row.getId(),
                        row.getLocation() != null ? row.getLocation().getId() : null,
                        row.getLocation() != null ? row.getLocation().getCode() : null,
                        row.getLotNumber(), row.getExpiryDate(), row.getQuantity(), suggested));
            }
            result.add(new PickSuggestionResponse(d.getId(), p.getId(), p.getSku(), p.getName(), p.isSerialManaged(),
                    remaining, locations));
        }
        return result;
    }

    @Override
    @Transactional
    public StockOutResponse pick(Long receiptId, PickGoodsRequest req) {
        StockOutReceipt receipt = findPickable(receiptId);
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), receipt.getWarehouse().getId());
        StockOutReceiptDetail detail = receipt.getDetails().stream()
                .filter(d -> d.getId().equals(req.detailId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Dòng phiếu xuất", req.detailId()));
        Product product = detail.getProduct();
        User picker = userRepository.getReferenceById(CurrentUser.id());

        if (product.isSerialManaged()) {
            if (req.serialNumbers() == null || req.serialNumbers().isEmpty()) {
                throw new BusinessException("Sản phẩm " + product.getSku() + " cần quét serial khi lấy hàng");
            }
            for (String raw : req.serialNumbers()) {
                ProductSerial serial = serialRepository.findBySerialNumber(raw.trim())
                        .orElseThrow(() -> new NotFoundException("Serial", raw));
                validateSerial(serial, product, receipt.getWarehouse());
                if (detail.getSerialAllocations().size() >= detail.getQuantity()) {
                    throw new BusinessException("Đã lấy đủ " + detail.getQuantity() + " cho " + product.getSku());
                }
                StockOutSerialAllocation allocation = new StockOutSerialAllocation();
                allocation.setDetail(detail);
                allocation.setSerial(serial);
                allocation.setPickedBy(picker);
                detail.getSerialAllocations().add(allocation);
                serial.setStatus(SerialStatus.RESERVED);
            }
            detail.setPickedQuantity(detail.getSerialAllocations().size());
        } else {
            if (req.quantity() > detail.getQuantity()) {
                throw new BusinessException("Số lượng lấy vượt quá số lượng trên phiếu (" + detail.getQuantity() + ")");
            }
            detail.setPickedQuantity(req.quantity());
        }

        receipt.setPickedBy(picker);
        approvalService.changeStatus(receipt, ReferenceType.STOCK_OUT, receipt.getWarehouse().getId(),
                ReceiptStatus.PICKING, null);
        return mapper.toStockOutResponse(receipt);
    }

    private static void validateSerial(ProductSerial serial, Product product, Warehouse warehouse) {
        if (!serial.getProduct().getId().equals(product.getId())) {
            throw new BusinessException("Serial " + serial.getSerialNumber() + " không thuộc sản phẩm " + product.getSku());
        }
        if (serial.getWarehouse() == null || !serial.getWarehouse().getId().equals(warehouse.getId())) {
            throw new BusinessException("Serial " + serial.getSerialNumber() + " không nằm trong kho xuất");
        }
        if (serial.getStatus() != SerialStatus.IN_STOCK) {
            throw new BusinessException("Serial " + serial.getSerialNumber() + " đang ở trạng thái " + serial.getStatus());
        }
    }

    private StockOutReceipt findPickable(Long id) {
        StockOutReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Phiếu xuất", id));
        if (receipt.getStatus() != ReceiptStatus.APPROVED && receipt.getStatus() != ReceiptStatus.PICKING) {
            throw new BusinessException("Phiếu xuất chưa được duyệt hoặc đã kết thúc");
        }
        return receipt;
    }
}
