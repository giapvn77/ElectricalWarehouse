package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.AdjustmentRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.request.ThresholdRequest;
import com.electricalwarehouse.dto.response.AdjustmentResponse;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.InventoryResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.ThresholdResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    /** Tồn kho thời gian thực theo vị trí/lô. */
    @GetMapping
    public ApiResponse<PagedResult<InventoryResponse>> search(@RequestParam(required = false) Long warehouseId,
                                                              @RequestParam(required = false) String keyword,
                                                              @PageableDefault(size = 50, sort = "product.sku") Pageable pageable) {
        return ApiResponse.ok(inventoryService.search(warehouseId, keyword, pageable));
    }

    @GetMapping("/by-product")
    public ApiResponse<List<InventoryResponse>> byProduct(@RequestParam Long warehouseId, @RequestParam Long productId) {
        return ApiResponse.ok(inventoryService.getByProduct(warehouseId, productId));
    }

    @GetMapping("/available")
    public ApiResponse<Integer> available(@RequestParam Long warehouseId, @RequestParam Long productId) {
        return ApiResponse.ok(inventoryService.getAvailableQuantity(warehouseId, productId));
    }

    @GetMapping("/adjustments")
    public ApiResponse<PagedResult<AdjustmentResponse>> adjustments(@RequestParam(required = false) ReceiptStatus status,
                                                                    @RequestParam(required = false) Long warehouseId,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(inventoryService.searchAdjustments(status, warehouseId, keyword, pageable));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ApiResponse<AdjustmentResponse> createAdjustment(@Valid @RequestBody AdjustmentRequest request) {
        return ApiResponse.ok(inventoryService.createAdjustment(request));
    }

    @PostMapping("/adjustments/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<AdjustmentResponse> approveAdjustment(@PathVariable Long id) {
        return ApiResponse.ok(inventoryService.approveAdjustment(id));
    }

    @PostMapping("/adjustments/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<AdjustmentResponse> rejectAdjustment(@PathVariable Long id,
                                                            @Valid @RequestBody RejectRequest request) {
        return ApiResponse.ok(inventoryService.rejectAdjustment(id, request));
    }

    @GetMapping("/thresholds")
    public ApiResponse<List<ThresholdResponse>> thresholds(@RequestParam(required = false) Long warehouseId) {
        return ApiResponse.ok(inventoryService.getThresholds(warehouseId));
    }

    @PostMapping("/thresholds")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<ThresholdResponse> saveThreshold(@Valid @RequestBody ThresholdRequest request) {
        return ApiResponse.ok(inventoryService.saveThreshold(request));
    }

    @DeleteMapping("/thresholds/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<Void> deleteThreshold(@PathVariable Long id) {
        inventoryService.deleteThreshold(id);
        return ApiResponse.ok();
    }
}
