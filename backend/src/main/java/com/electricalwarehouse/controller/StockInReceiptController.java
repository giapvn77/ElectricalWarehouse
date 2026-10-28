package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.CreateStockInRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockInResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.service.StockInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Phiếu nhập kho. "complete" posts inventory after goods (and serials) are received. */
@RestController
@RequestMapping("/api/stock-in")
@RequiredArgsConstructor
public class StockInReceiptController {
    private final StockInService service;

    @GetMapping
    public ApiResponse<PagedResult<StockInResponse>> search(@RequestParam(required = false) ReceiptStatus status,
                                                       @RequestParam(required = false) Long warehouseId,
                                                       @RequestParam(required = false) String keyword,
                                                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(service.search(status, warehouseId, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<StockInResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STOCK_IN_CREATE')")
    public ApiResponse<StockInResponse> create(@Valid @RequestBody CreateStockInRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_IN_CREATE')")
    public ApiResponse<StockInResponse> update(@PathVariable Long id, @Valid @RequestBody CreateStockInRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('STOCK_IN_CREATE')")
    public ApiResponse<StockInResponse> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('STOCK_IN_APPROVE')")
    public ApiResponse<StockInResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('STOCK_IN_APPROVE')")
    public ApiResponse<StockInResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ApiResponse.ok(service.reject(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<StockInResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('STOCK_IN_CREATE')")
    public ApiResponse<StockInResponse> complete(@PathVariable Long id) {
        return ApiResponse.ok(service.complete(id));
    }
}
