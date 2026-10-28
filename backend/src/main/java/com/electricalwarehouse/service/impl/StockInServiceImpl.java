package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.CreateStockInRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockInResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.SerialStatus;
import com.electricalwarehouse.entity.enums.TransactionType;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.ProductSerialRepository;
import com.electricalwarehouse.repository.StockInReceiptRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.InventoryService;
import com.electricalwarehouse.service.Lookup;
import com.electricalwarehouse.service.StockInService;
import com.electricalwarehouse.service.StockMovement;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import com.electricalwarehouse.service.approval.ApprovalService;
import com.electricalwarehouse.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StockInServiceImpl implements StockInService {

    private static final ReferenceType TYPE = ReferenceType.STOCK_IN;

    private final StockInReceiptRepository receiptRepository;
    private final ProductSerialRepository serialRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ApprovalService approvalService;
    private final ApprovalPolicy approvalPolicy;
    private final Lookup lookup;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<StockInResponse> search(ReceiptStatus status, Long warehouseId, String keyword,
                                               Pageable pageable) {
        return PagedResult.of(receiptRepository.search(status, warehouseId, InventoryServiceImpl.blankToNull(keyword),
                pageable), mapper::toStockInResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StockInResponse get(Long id) {
        return mapper.toStockInResponse(find(id));
    }

    @Override
    @Transactional
    public StockInResponse create(CreateStockInRequest req) {
        StockInReceipt receipt = new StockInReceipt();
        receipt.setCode(CodeGenerator.next(CodeGenerator.STOCK_IN));
        receipt.setCreatedBy(userRepository.getReferenceById(CurrentUser.id()));
        apply(receipt, req);
        return mapper.toStockInResponse(receiptRepository.save(receipt));
    }

    @Override
    @Transactional
    public StockInResponse update(Long id, CreateStockInRequest req) {
        StockInReceipt receipt = find(id);
        approvalPolicy.checkCanEdit(CurrentUser.get(), receipt);
        receipt.getDetails().clear();
        receiptRepository.flush();
        apply(receipt, req);
        return mapper.toStockInResponse(receipt);
    }

    private void apply(StockInReceipt receipt, CreateStockInRequest req) {
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), req.warehouseId());
        Warehouse warehouse = lookup.warehouse(req.warehouseId());
        receipt.setWarehouse(warehouse);
        receipt.setSupplier(lookup.supplier(req.supplierId()));
        receipt.setReceiptDate(req.receiptDate() != null ? req.receiptDate() : LocalDate.now());
        receipt.setInvoiceNumber(req.invoiceNumber());
        receipt.setNote(req.note());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateStockInRequest.Item item : req.items()) {
            Product product = lookup.product(item.productId());
            if (!product.isActive()) {
                throw new BusinessException("Sản phẩm " + product.getSku() + " đã ngừng kinh doanh");
            }
            if (product.isExpiryManaged() && item.expiryDate() == null) {
                throw new BusinessException("Sản phẩm " + product.getSku() + " bắt buộc nhập hạn sử dụng");
            }
            StockInReceiptDetail d = new StockInReceiptDetail();
            d.setProduct(product);
            d.setLocation(lookup.location(item.locationId(), warehouse.getId()));
            d.setQuantity(item.quantity());
            d.setUnitPrice(item.unitPrice() != null ? item.unitPrice() : product.getCostPrice());
            d.setLotNumber(item.lotNumber());
            d.setExpiryDate(item.expiryDate());
            receipt.addDetail(d);
            total = total.add(d.getUnitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        receipt.setTotalAmount(total);
    }

    @Override
    @Transactional
    public StockInResponse submit(Long id) {
        StockInReceipt receipt = find(id);
        approvalService.submit(receipt, TYPE, receipt.getWarehouse().getId());
        return mapper.toStockInResponse(receipt);
    }

    @Override
    @Transactional
    public StockInResponse approve(Long id) {
        StockInReceipt receipt = find(id);
        approvalService.approve(receipt, TYPE, receipt.getWarehouse().getId());
        return mapper.toStockInResponse(receipt);
    }

    @Override
    @Transactional
    public StockInResponse reject(Long id, RejectRequest request) {
        StockInReceipt receipt = find(id);
        approvalService.reject(receipt, TYPE, receipt.getWarehouse().getId(), request.reason());
        return mapper.toStockInResponse(receipt);
    }

    @Override
    @Transactional
    public StockInResponse cancel(Long id) {
        StockInReceipt receipt = find(id);
        approvalService.cancel(receipt, TYPE, receipt.getWarehouse().getId());
        return mapper.toStockInResponse(receipt);
    }

    /** Goods physically received: post inventory and activate scanned serials. */
    @Override
    @Transactional
    public StockInResponse complete(Long id) {
        StockInReceipt receipt = find(id);
        if (receipt.getStatus() != ReceiptStatus.APPROVED) {
            throw new BusinessException("Chỉ hoàn tất nhập kho cho phiếu đã duyệt");
        }
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), receipt.getWarehouse().getId());

        for (StockInReceiptDetail d : receipt.getDetails()) {
            Product p = d.getProduct();
            if (p.isSerialManaged() && d.getScannedQuantity() != d.getQuantity()) {
                throw new BusinessException("Sản phẩm " + p.getSku() + " mới quét " + d.getScannedQuantity() + "/"
                        + d.getQuantity() + " serial");
            }
            inventoryService.increase(new StockMovement(receipt.getWarehouse(), p, d.getLocation(), d.getLotNumber(),
                    d.getExpiryDate(), d.getQuantity(), TransactionType.IN, TYPE, receipt.getId(), receipt.getCode(),
                    d.getUnitPrice(), null));
            if (p.isSerialManaged()) {
                serialRepository.findByStockInDetailId(d.getId()).forEach(s -> s.setStatus(SerialStatus.IN_STOCK));
            }
        }
        approvalService.changeStatus(receipt, TYPE, receipt.getWarehouse().getId(), ReceiptStatus.COMPLETED, null);
        return mapper.toStockInResponse(receipt);
    }

    private StockInReceipt find(Long id) {
        return receiptRepository.findById(id).orElseThrow(() -> new NotFoundException("Phiếu nhập", id));
    }
}
