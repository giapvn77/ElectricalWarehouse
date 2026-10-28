package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.AdjustmentRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.request.ThresholdRequest;
import com.electricalwarehouse.dto.response.AdjustmentResponse;
import com.electricalwarehouse.dto.response.InventoryResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.ThresholdResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.TransactionType;
import com.electricalwarehouse.event.StockChangedEvent;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.InventoryAdjustmentRepository;
import com.electricalwarehouse.repository.InventoryRepository;
import com.electricalwarehouse.repository.InventoryThresholdRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.InventoryService;
import com.electricalwarehouse.service.Lookup;
import com.electricalwarehouse.service.StockMovement;
import com.electricalwarehouse.service.StockTransactionService;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import com.electricalwarehouse.service.approval.ApprovalService;
import com.electricalwarehouse.util.CodeGenerator;
import com.electricalwarehouse.util.FifoFefoCalculator;
import com.electricalwarehouse.util.FifoFefoCalculator.Allocation;
import com.electricalwarehouse.util.FifoFefoCalculator.Plan;
import com.electricalwarehouse.util.FifoFefoCalculator.Pool;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final InventoryThresholdRepository thresholdRepository;
    private final UserRepository userRepository;
    private final StockTransactionService transactionService;
    private final FifoFefoCalculator calculator;
    private final ApprovalService approvalService;
    private final ApprovalPolicy approvalPolicy;
    private final ApplicationEventPublisher events;
    private final Lookup lookup;
    private final DTOMapper mapper;

    // ================= queries =================

    @Override
    @Transactional(readOnly = true)
    public PagedResult<InventoryResponse> search(Long warehouseId, String keyword, Pageable pageable) {
        return PagedResult.of(inventoryRepository.search(warehouseId, blankToNull(keyword), pageable),
                mapper::toInventoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getByProduct(Long warehouseId, Long productId) {
        Product product = lookup.product(productId);
        return calculator.sortForPicking(inventoryRepository.findByWarehouseIdAndProductId(warehouseId, productId),
                        product.isExpiryManaged()).stream()
                .filter(i -> i.getQuantity() > 0)
                .map(mapper::toInventoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableQuantity(Long warehouseId, Long productId) {
        return inventoryRepository.sumAvailable(warehouseId, productId);
    }

    // ================= posting =================

    @Override
    @Transactional
    public Inventory increase(StockMovement m) {
        requirePositive(m.quantity());
        Long locationId = m.location() != null ? m.location().getId() : null;
        Inventory row = inventoryRepository.findByWarehouseIdAndProductIdAndLocationIdAndLotNumber(
                m.warehouse().getId(), m.product().getId(), locationId, m.lotNumber()).orElseGet(() -> {
            Inventory created = new Inventory();
            created.setWarehouse(m.warehouse());
            created.setProduct(m.product());
            created.setLocation(m.location());
            created.setLotNumber(m.lotNumber());
            created.setExpiryDate(m.expiryDate());
            return created;
        });
        row.setQuantity(row.getQuantity() + m.quantity());
        inventoryRepository.save(row);
        afterChange(m, m.location(), m.lotNumber(), m.quantity());
        return row;
    }

    @Override
    @Transactional
    public void decreaseAt(StockMovement m) {
        requirePositive(m.quantity());
        Long locationId = m.location() != null ? m.location().getId() : null;
        Inventory row = inventoryRepository.findByWarehouseIdAndProductIdAndLocationIdAndLotNumber(
                        m.warehouse().getId(), m.product().getId(), locationId, m.lotNumber())
                .orElseThrow(() -> new BusinessException("Không có tồn kho của " + m.product().getSku()
                        + " tại vị trí/lô đã chọn"));
        if (row.getAvailableQuantity() < m.quantity()) {
            throw new BusinessException("Tồn khả dụng của " + m.product().getSku() + " tại vị trí/lô này chỉ còn "
                    + row.getAvailableQuantity());
        }
        row.setQuantity(row.getQuantity() - m.quantity());
        afterChange(m, row.getLocation(), row.getLotNumber(), -m.quantity());
    }

    @Override
    @Transactional
    public List<Allocation> decrease(StockMovement m, Pool pool) {
        requirePositive(m.quantity());
        Plan plan = plan(m.warehouse(), m.product(), m.quantity(), pool);
        for (Allocation a : plan.allocations()) {
            Inventory row = a.inventory();
            row.setQuantity(row.getQuantity() - a.quantity());
            if (pool == Pool.RESERVED) {
                row.setReservedQuantity(row.getReservedQuantity() - a.quantity());
            }
            afterChange(m.withQuantity(a.quantity()), row.getLocation(), row.getLotNumber(), -a.quantity());
        }
        return plan.allocations();
    }

    @Override
    @Transactional
    public void reserve(Warehouse warehouse, Product product, int quantity) {
        plan(warehouse, product, quantity, Pool.AVAILABLE).allocations()
                .forEach(a -> a.inventory().setReservedQuantity(a.inventory().getReservedQuantity() + a.quantity()));
    }

    @Override
    @Transactional
    public void releaseReservation(Warehouse warehouse, Product product, int quantity) {
        // Reservations are fungible per product, so releasing in picking order keeps totals consistent.
        Plan plan = calculator.allocate(inventoryRepository.findByWarehouseIdAndProductId(warehouse.getId(),
                product.getId()), quantity, product.isExpiryManaged(), Pool.RESERVED);
        plan.allocations().forEach(a ->
                a.inventory().setReservedQuantity(a.inventory().getReservedQuantity() - a.quantity()));
    }

    private Plan plan(Warehouse warehouse, Product product, int quantity, Pool pool) {
        Plan plan = calculator.allocate(inventoryRepository.findByWarehouseIdAndProductId(warehouse.getId(),
                product.getId()), quantity, product.isExpiryManaged(), pool);
        if (!plan.isComplete()) {
            throw new BusinessException("Không đủ tồn kho cho " + product.getSku() + " tại kho " + warehouse.getCode()
                    + ": thiếu " + plan.shortage());
        }
        return plan;
    }

    private void afterChange(StockMovement m, StorageLocation location, String lot, int signedQuantity) {
        inventoryRepository.flush();
        int balance = inventoryRepository.sumQuantity(m.warehouse().getId(), m.product().getId());
        transactionService.record(m, location, lot, signedQuantity, balance);
        events.publishEvent(new StockChangedEvent(m.warehouse().getId(), m.product().getId(), signedQuantity, balance,
                m.type()));
    }

    // ================= adjustments =================

    @Override
    @Transactional(readOnly = true)
    public PagedResult<AdjustmentResponse> searchAdjustments(ReceiptStatus status, Long warehouseId, String keyword,
                                                            Pageable pageable) {
        return PagedResult.of(adjustmentRepository.search(status, warehouseId, blankToNull(keyword), pageable),
                mapper::toAdjustmentResponse);
    }

    @Override
    @Transactional
    public AdjustmentResponse createAdjustment(AdjustmentRequest req) {
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), req.warehouseId());
        Warehouse warehouse = lookup.warehouse(req.warehouseId());
        Product product = lookup.product(req.productId());
        StorageLocation location = lookup.location(req.locationId(), warehouse.getId());
        int systemQty = inventoryRepository.findByWarehouseIdAndProductIdAndLocationIdAndLotNumber(
                warehouse.getId(), product.getId(), req.locationId(), req.lotNumber())
                .map(Inventory::getQuantity).orElse(0);
        if (systemQty == req.actualQuantity()) {
            throw new BusinessException("Số lượng thực tế bằng tồn hệ thống, không cần điều chỉnh");
        }

        InventoryAdjustment adj = new InventoryAdjustment();
        adj.setCode(CodeGenerator.next(CodeGenerator.ADJUSTMENT));
        adj.setWarehouse(warehouse);
        adj.setProduct(product);
        adj.setLocation(location);
        adj.setLotNumber(req.lotNumber());
        adj.setSystemQuantity(systemQty);
        adj.setActualQuantity(req.actualQuantity());
        adj.setReason(req.reason());
        adj.setCreatedBy(userRepository.getReferenceById(CurrentUser.id()));
        adjustmentRepository.save(adj);
        // Adjustments need no drafting step: they go straight to the approval queue.
        approvalService.submit(adj, ReferenceType.ADJUSTMENT, warehouse.getId());
        return mapper.toAdjustmentResponse(adj);
    }

    @Override
    @Transactional
    public AdjustmentResponse approveAdjustment(Long id) {
        InventoryAdjustment adj = findAdjustment(id);
        approvalService.approve(adj, ReferenceType.ADJUSTMENT, adj.getWarehouse().getId());

        // Re-read the current quantity: stock may have moved since the count was recorded.
        int current = inventoryRepository.findByWarehouseIdAndProductIdAndLocationIdAndLotNumber(
                        adj.getWarehouse().getId(), adj.getProduct().getId(),
                        adj.getLocation() != null ? adj.getLocation().getId() : null, adj.getLotNumber())
                .map(Inventory::getQuantity).orElse(0);
        int delta = adj.getActualQuantity() - current;
        if (delta != 0) {
            StockMovement m = new StockMovement(adj.getWarehouse(), adj.getProduct(), adj.getLocation(),
                    adj.getLotNumber(), null, Math.abs(delta), TransactionType.ADJUST, ReferenceType.ADJUSTMENT,
                    adj.getId(), adj.getCode(), null, adj.getReason());
            if (delta > 0) {
                increase(m);
            } else {
                decreaseAt(m);
            }
        }
        approvalService.changeStatus(adj, ReferenceType.ADJUSTMENT, adj.getWarehouse().getId(),
                ReceiptStatus.COMPLETED, null);
        return mapper.toAdjustmentResponse(adj);
    }

    @Override
    @Transactional
    public AdjustmentResponse rejectAdjustment(Long id, RejectRequest request) {
        InventoryAdjustment adj = findAdjustment(id);
        approvalService.reject(adj, ReferenceType.ADJUSTMENT, adj.getWarehouse().getId(), request.reason());
        return mapper.toAdjustmentResponse(adj);
    }

    private InventoryAdjustment findAdjustment(Long id) {
        return adjustmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Phiếu kiểm kê", id));
    }

    // ================= thresholds =================

    @Override
    @Transactional(readOnly = true)
    public List<ThresholdResponse> getThresholds(Long warehouseId) {
        List<InventoryThreshold> list = warehouseId != null
                ? thresholdRepository.findByWarehouseId(warehouseId)
                : thresholdRepository.findAll();
        return list.stream().map(mapper::toThresholdResponse).toList();
    }

    @Override
    @Transactional
    public ThresholdResponse saveThreshold(ThresholdRequest req) {
        if (req.maxQuantity() < req.minQuantity()) {
            throw new BusinessException("Mức tối đa phải lớn hơn hoặc bằng mức tối thiểu");
        }
        InventoryThreshold t = thresholdRepository.findByWarehouseIdAndProductId(req.warehouseId(), req.productId())
                .orElseGet(InventoryThreshold::new);
        t.setWarehouse(lookup.warehouse(req.warehouseId()));
        t.setProduct(lookup.product(req.productId()));
        t.setMinQuantity(req.minQuantity());
        t.setMaxQuantity(req.maxQuantity());
        return mapper.toThresholdResponse(thresholdRepository.save(t));
    }

    @Override
    @Transactional
    public void deleteThreshold(Long id) {
        thresholdRepository.deleteById(id);
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Số lượng phải lớn hơn 0");
        }
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
