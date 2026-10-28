package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.request.TransferRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.TransferResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.SerialStatus;
import com.electricalwarehouse.entity.enums.TransactionType;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.ProductSerialRepository;
import com.electricalwarehouse.repository.StockTransferRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.InventoryService;
import com.electricalwarehouse.service.Lookup;
import com.electricalwarehouse.service.StockMovement;
import com.electricalwarehouse.service.StockTransferService;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import com.electricalwarehouse.service.approval.ApprovalService;
import com.electricalwarehouse.util.CodeGenerator;
import com.electricalwarehouse.util.FifoFefoCalculator.Pool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockTransferServiceImpl implements StockTransferService {

    private static final ReferenceType TYPE = ReferenceType.TRANSFER;

    private final StockTransferRepository transferRepository;
    private final ProductSerialRepository serialRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ApprovalService approvalService;
    private final ApprovalPolicy approvalPolicy;
    private final Lookup lookup;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<TransferResponse> search(ReceiptStatus status, Long warehouseId, String keyword,
                                                Pageable pageable) {
        return PagedResult.of(transferRepository.search(status, warehouseId, InventoryServiceImpl.blankToNull(keyword),
                pageable), mapper::toTransferResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse get(Long id) {
        return mapper.toTransferResponse(find(id));
    }

    @Override
    @Transactional
    public TransferResponse create(TransferRequest req) {
        if (req.fromWarehouseId().equals(req.toWarehouseId())) {
            throw new BusinessException("Kho nguồn và kho đích phải khác nhau");
        }
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), req.fromWarehouseId());
        Warehouse from = lookup.warehouse(req.fromWarehouseId());
        Warehouse to = lookup.warehouse(req.toWarehouseId());

        StockTransfer transfer = new StockTransfer();
        transfer.setCode(CodeGenerator.next(CodeGenerator.TRANSFER));
        transfer.setFromWarehouse(from);
        transfer.setToWarehouse(to);
        transfer.setNote(req.note());
        transfer.setCreatedBy(userRepository.getReferenceById(CurrentUser.id()));

        for (TransferRequest.Item item : req.items()) {
            Product product = lookup.product(item.productId());
            int available = inventoryService.getAvailableQuantity(from.getId(), product.getId());
            if (available < item.quantity()) {
                throw new BusinessException("Tồn khả dụng của " + product.getSku() + " chỉ còn " + available);
            }
            StockTransferDetail d = new StockTransferDetail();
            d.setProduct(product);
            d.setQuantity(item.quantity());
            d.setToLocation(lookup.location(item.toLocationId(), to.getId()));
            if (product.isSerialManaged()) {
                List<String> serials = normalize(item.serialNumbers());
                if (serials.size() != item.quantity()) {
                    throw new BusinessException("Sản phẩm " + product.getSku() + " cần đúng " + item.quantity() + " serial");
                }
                serials.forEach(s -> requireSerialInStock(s, product, from));
                d.setSerialNumbers(String.join(",", serials));
            }
            transfer.addDetail(d);
        }
        return mapper.toTransferResponse(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public TransferResponse submit(Long id) {
        StockTransfer t = find(id);
        approvalService.submit(t, TYPE, t.getFromWarehouse().getId());
        return mapper.toTransferResponse(t);
    }

    /** Goods leave the source warehouse on approval and are in transit until received. */
    @Override
    @Transactional
    public TransferResponse approve(Long id) {
        StockTransfer t = find(id);
        approvalService.approve(t, TYPE, t.getFromWarehouse().getId());
        for (StockTransferDetail d : t.getDetails()) {
            Product p = d.getProduct();
            StockMovement m = StockMovement.of(t.getFromWarehouse(), p, d.getQuantity(), TransactionType.TRANSFER_OUT,
                    TYPE, t.getId(), t.getCode());
            if (p.isSerialManaged()) {
                for (String sn : split(d.getSerialNumbers())) {
                    ProductSerial s = requireSerialInStock(sn, p, t.getFromWarehouse());
                    inventoryService.decreaseAt(m.withQuantity(1).at(s.getLocation(), s.getLotNumber(), null));
                    s.setStatus(SerialStatus.RESERVED);
                    s.setLocation(null);
                }
            } else {
                inventoryService.decrease(m, Pool.AVAILABLE);
            }
        }
        return mapper.toTransferResponse(t);
    }

    @Override
    @Transactional
    public TransferResponse reject(Long id, RejectRequest request) {
        StockTransfer t = find(id);
        approvalService.reject(t, TYPE, t.getFromWarehouse().getId(), request.reason());
        return mapper.toTransferResponse(t);
    }

    @Override
    @Transactional
    public TransferResponse cancel(Long id) {
        StockTransfer t = find(id);
        approvalService.cancel(t, TYPE, t.getFromWarehouse().getId());
        return mapper.toTransferResponse(t);
    }

    @Override
    @Transactional
    public TransferResponse receive(Long id) {
        StockTransfer t = find(id);
        if (t.getStatus() != ReceiptStatus.APPROVED) {
            throw new BusinessException("Chỉ nhận hàng cho phiếu chuyển kho đã duyệt");
        }
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), t.getToWarehouse().getId());
        for (StockTransferDetail d : t.getDetails()) {
            Product p = d.getProduct();
            inventoryService.increase(StockMovement.of(t.getToWarehouse(), p, d.getQuantity(),
                    TransactionType.TRANSFER_IN, TYPE, t.getId(), t.getCode()).at(d.getToLocation(), null, null));
            if (p.isSerialManaged()) {
                for (String sn : split(d.getSerialNumbers())) {
                    serialRepository.findBySerialNumber(sn).ifPresent(s -> {
                        s.setWarehouse(t.getToWarehouse());
                        s.setLocation(d.getToLocation());
                        s.setLotNumber(null);
                        s.setStatus(SerialStatus.IN_STOCK);
                    });
                }
            }
        }
        t.setReceivedAt(LocalDateTime.now());
        t.setReceivedBy(userRepository.getReferenceById(CurrentUser.id()));
        approvalService.changeStatus(t, TYPE, t.getFromWarehouse().getId(), ReceiptStatus.COMPLETED, null);
        return mapper.toTransferResponse(t);
    }

    private ProductSerial requireSerialInStock(String serialNumber, Product product, Warehouse warehouse) {
        ProductSerial s = serialRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new NotFoundException("Serial", serialNumber));
        if (!s.getProduct().getId().equals(product.getId()) || s.getWarehouse() == null
                || !s.getWarehouse().getId().equals(warehouse.getId()) || s.getStatus() != SerialStatus.IN_STOCK) {
            throw new BusinessException("Serial " + serialNumber + " không có sẵn trong kho nguồn");
        }
        return s;
    }

    private static List<String> normalize(List<String> serials) {
        return serials == null ? List.of()
                : List.copyOf(new LinkedHashSet<>(serials.stream().map(String::trim).filter(s -> !s.isEmpty()).toList()));
    }

    private static List<String> split(String serials) {
        return serials == null || serials.isBlank() ? List.of() : Arrays.asList(serials.split(","));
    }

    private StockTransfer find(Long id) {
        return transferRepository.findById(id).orElseThrow(() -> new NotFoundException("Phiếu chuyển kho", id));
    }
}
