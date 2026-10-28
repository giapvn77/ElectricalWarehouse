package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.CreateStockOutRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockOutResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.entity.enums.SerialStatus;
import com.electricalwarehouse.entity.enums.TransactionType;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.StockOutReceiptRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.security.UserPrincipal;
import com.electricalwarehouse.service.InventoryService;
import com.electricalwarehouse.service.Lookup;
import com.electricalwarehouse.service.StockMovement;
import com.electricalwarehouse.service.StockOutService;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import com.electricalwarehouse.service.approval.ApprovalService;
import com.electricalwarehouse.util.CodeGenerator;
import com.electricalwarehouse.util.FifoFefoCalculator.Pool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockOutServiceImpl implements StockOutService {

    private static final ReferenceType TYPE = ReferenceType.STOCK_OUT;

    private final StockOutReceiptRepository receiptRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ApprovalService approvalService;
    private final ApprovalPolicy approvalPolicy;
    private final Lookup lookup;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<StockOutResponse> search(ReceiptStatus status, Long warehouseId, String keyword,
                                                Pageable pageable) {
        return PagedResult.of(receiptRepository.search(status, warehouseId, InventoryServiceImpl.blankToNull(keyword),
                pageable), mapper::toStockOutResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StockOutResponse get(Long id) {
        return mapper.toStockOutResponse(find(id));
    }

    @Override
    @Transactional
    public StockOutResponse create(CreateStockOutRequest req) {
        StockOutReceipt receipt = new StockOutReceipt();
        receipt.setCode(CodeGenerator.next(CodeGenerator.STOCK_OUT));
        receipt.setCreatedBy(userRepository.getReferenceById(CurrentUser.id()));
        apply(receipt, req);
        return mapper.toStockOutResponse(receiptRepository.save(receipt));
    }

    @Override
    @Transactional
    public StockOutResponse update(Long id, CreateStockOutRequest req) {
        StockOutReceipt receipt = find(id);
        approvalPolicy.checkCanEdit(CurrentUser.get(), receipt);
        receipt.getDetails().clear();
        receiptRepository.flush();
        apply(receipt, req);
        return mapper.toStockOutResponse(receipt);
    }

    private void apply(StockOutReceipt receipt, CreateStockOutRequest req) {
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), req.warehouseId());
        Warehouse warehouse = lookup.warehouse(req.warehouseId());
        receipt.setWarehouse(warehouse);
        receipt.setCustomer(lookup.customer(req.customerId()));
        receipt.setOutType(req.outType());
        receipt.setReceiptDate(req.receiptDate() != null ? req.receiptDate() : LocalDate.now());
        receipt.setNote(req.note());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateStockOutRequest.Item item : req.items()) {
            Product product = lookup.product(item.productId());
            StockOutReceiptDetail d = new StockOutReceiptDetail();
            d.setProduct(product);
            d.setQuantity(item.quantity());
            d.setUnitPrice(item.unitPrice() != null ? item.unitPrice() : product.getSalePrice());
            receipt.addDetail(d);
            total = total.add(d.getUnitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        checkAvailability(receipt);
        receipt.setTotalAmount(total);
    }

    /** Early feedback for the creator; the binding check happens again when stock is reserved. */
    private void checkAvailability(StockOutReceipt receipt) {
        Map<Product, Integer> needed = new HashMap<>();
        receipt.getDetails().forEach(d -> needed.merge(d.getProduct(), d.getQuantity(), Integer::sum));
        needed.forEach((product, qty) -> {
            int available = inventoryService.getAvailableQuantity(receipt.getWarehouse().getId(), product.getId());
            if (available < qty) {
                throw new BusinessException("Tồn khả dụng của " + product.getSku() + " chỉ còn " + available
                        + ", không đủ " + qty);
            }
        });
    }

    @Override
    @Transactional
    public StockOutResponse submit(Long id) {
        StockOutReceipt receipt = find(id);
        checkAvailability(receipt);
        approvalService.submit(receipt, TYPE, receipt.getWarehouse().getId());
        return mapper.toStockOutResponse(receipt);
    }

    /** Approval reserves stock so it cannot be promised twice while goods are picked. */
    @Override
    @Transactional
    public StockOutResponse approve(Long id) {
        StockOutReceipt receipt = find(id);
        approvalService.approve(receipt, TYPE, receipt.getWarehouse().getId());
        receipt.getDetails().forEach(d ->
                inventoryService.reserve(receipt.getWarehouse(), d.getProduct(), d.getQuantity()));
        return mapper.toStockOutResponse(receipt);
    }

    @Override
    @Transactional
    public StockOutResponse reject(Long id, RejectRequest request) {
        StockOutReceipt receipt = find(id);
        approvalService.reject(receipt, TYPE, receipt.getWarehouse().getId(), request.reason());
        return mapper.toStockOutResponse(receipt);
    }

    /**
     * Drafts/pending receipts are cancelled by their creator. Approved or picking receipts hold
     * reservations, so only a warehouse manager may cancel them and the reservation is released.
     */
    @Override
    @Transactional
    public StockOutResponse cancel(Long id) {
        StockOutReceipt receipt = find(id);
        ReceiptStatus status = receipt.getStatus();
        if (status == ReceiptStatus.APPROVED || status == ReceiptStatus.PICKING) {
            UserPrincipal user = CurrentUser.get();
            if (!user.hasRole(RoleCode.ADMIN) && !user.hasRole(RoleCode.QLK)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "Chỉ Quản lý kho được hủy phiếu đã duyệt");
            }
            for (StockOutReceiptDetail d : receipt.getDetails()) {
                inventoryService.releaseReservation(receipt.getWarehouse(), d.getProduct(), d.getQuantity());
                d.getSerialAllocations().forEach(a -> a.getSerial().setStatus(SerialStatus.IN_STOCK));
                d.getSerialAllocations().clear();
                d.setPickedQuantity(0);
            }
            approvalService.changeStatus(receipt, TYPE, receipt.getWarehouse().getId(), ReceiptStatus.CANCELLED,
                    "Hủy sau duyệt");
        } else {
            approvalService.cancel(receipt, TYPE, receipt.getWarehouse().getId());
        }
        return mapper.toStockOutResponse(receipt);
    }

    /** All lines picked: take the goods out of stock. */
    @Override
    @Transactional
    public StockOutResponse complete(Long id) {
        StockOutReceipt receipt = find(id);
        if (receipt.getStatus() != ReceiptStatus.PICKING) {
            throw new BusinessException("Chỉ hoàn tất xuất kho cho phiếu đang lấy hàng");
        }
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), receipt.getWarehouse().getId());
        Warehouse wh = receipt.getWarehouse();

        for (StockOutReceiptDetail d : receipt.getDetails()) {
            Product p = d.getProduct();
            if (d.getPickedQuantity() != d.getQuantity()) {
                throw new BusinessException("Sản phẩm " + p.getSku() + " mới lấy " + d.getPickedQuantity() + "/"
                        + d.getQuantity());
            }
            StockMovement m = StockMovement.of(wh, p, d.getQuantity(), TransactionType.OUT, TYPE, receipt.getId(),
                    receipt.getCode());
            if (p.isSerialManaged()) {
                // Serials pin the exact location/lot: swap the FIFO reservation for those rows.
                inventoryService.releaseReservation(wh, p, d.getQuantity());
                Map<List<Object>, List<StockOutSerialAllocation>> byRow = d.getSerialAllocations().stream()
                        .collect(Collectors.groupingBy(a -> java.util.Arrays.asList(
                                a.getSerial().getLocation() != null ? a.getSerial().getLocation().getId() : null,
                                a.getSerial().getLotNumber())));
                byRow.values().forEach(group -> {
                    ProductSerial first = group.get(0).getSerial();
                    inventoryService.decreaseAt(m.withQuantity(group.size()).at(first.getLocation(),
                            first.getLotNumber(), null));
                });
                d.getSerialAllocations().forEach(a -> {
                    a.getSerial().setStatus(SerialStatus.SOLD);
                    a.getSerial().setLocation(null);
                });
            } else {
                inventoryService.decrease(m, Pool.RESERVED);
            }
        }
        approvalService.changeStatus(receipt, TYPE, wh.getId(), ReceiptStatus.COMPLETED, null);
        return mapper.toStockOutResponse(receipt);
    }

    private StockOutReceipt find(Long id) {
        return receiptRepository.findById(id).orElseThrow(() -> new NotFoundException("Phiếu xuất", id));
    }
}
