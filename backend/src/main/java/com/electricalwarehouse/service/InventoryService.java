package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.AdjustmentRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.request.ThresholdRequest;
import com.electricalwarehouse.dto.response.*;
import com.electricalwarehouse.entity.Inventory;
import com.electricalwarehouse.entity.Product;
import com.electricalwarehouse.entity.Warehouse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.util.FifoFefoCalculator;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    // ----- queries -----
    PagedResult<InventoryResponse> search(Long warehouseId, String keyword, Pageable pageable);

    List<InventoryResponse> getByProduct(Long warehouseId, Long productId);

    int getAvailableQuantity(Long warehouseId, Long productId);

    // ----- stock posting (called by other services inside their transaction) -----

    /** Adds stock to the row matching location + lot (created if missing). */
    Inventory increase(StockMovement movement);

    /** Removes stock from the exact row matching location + lot. */
    void decreaseAt(StockMovement movement);

    /** Removes stock across rows in FIFO/FEFO order, from the AVAILABLE or RESERVED pool. */
    List<FifoFefoCalculator.Allocation> decrease(StockMovement movement, FifoFefoCalculator.Pool pool);

    void reserve(Warehouse warehouse, Product product, int quantity);

    void releaseReservation(Warehouse warehouse, Product product, int quantity);

    // ----- adjustments (kiểm kê) -----
    PagedResult<AdjustmentResponse> searchAdjustments(ReceiptStatus status, Long warehouseId, String keyword,
                                                     Pageable pageable);

    AdjustmentResponse createAdjustment(AdjustmentRequest request);

    AdjustmentResponse approveAdjustment(Long id);

    AdjustmentResponse rejectAdjustment(Long id, RejectRequest request);

    // ----- Min-Max thresholds -----
    List<ThresholdResponse> getThresholds(Long warehouseId);

    ThresholdResponse saveThreshold(ThresholdRequest request);

    void deleteThreshold(Long id);
}
