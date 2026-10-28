package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.CreateStockOutRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockOutResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.service.StockOutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Phiếu xuất kho. "complete" deducts inventory after every line is picked. */
@RestController
@RequestMapping("/api/stock-out")
@RequiredArgsConstructor
public class StockOutReceiptController {
    private final StockOutService service;

    @GetMapping
    public ApiResponse<PagedResult<StockOutResponse>> search(@RequestParam(required = false) ReceiptStatus status,
                                                       @RequestParam(required = false) Long warehouseId,
                                                       @RequestParam(required = false) String keyword,
                                                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(service.search(status, warehouseId, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<StockOutResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STOCK_OUT_CREATE')")
    public ApiResponse<StockOutResponse> create(@Valid @RequestBody CreateStockOutRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_OUT_CREATE')")
    public ApiResponse<StockOutResponse> update(@PathVariable Long id, @Valid @RequestBody CreateStockOutRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('STOCK_OUT_CREATE')")
    public ApiResponse<StockOutResponse> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('STOCK_OUT_APPROVE')")
    public ApiResponse<StockOutResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('STOCK_OUT_APPROVE')")
    public ApiResponse<StockOutResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ApiResponse.ok(service.reject(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<StockOutResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('STOCK_OUT_PICK')")
    public ApiResponse<StockOutResponse> complete(@PathVariable Long id) {
        return ApiResponse.ok(service.complete(id));
    }
}
