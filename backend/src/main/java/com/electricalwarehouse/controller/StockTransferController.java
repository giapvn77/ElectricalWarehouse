package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.request.TransferRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.TransferResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Chuyển kho. "receive" is confirmed by the destination warehouse. */
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class StockTransferController {
    private final StockTransferService service;

    @GetMapping
    public ApiResponse<PagedResult<TransferResponse>> search(@RequestParam(required = false) ReceiptStatus status,
                                                       @RequestParam(required = false) Long warehouseId,
                                                       @RequestParam(required = false) String keyword,
                                                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(service.search(status, warehouseId, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TRANSFER_MANAGE')")
    public ApiResponse<TransferResponse> create(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('TRANSFER_MANAGE')")
    public ApiResponse<TransferResponse> submit(@PathVariable Long id) {
        return ApiResponse.ok(service.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<TransferResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<TransferResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return ApiResponse.ok(service.reject(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TransferResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(service.cancel(id));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('TRANSFER_MANAGE')")
    public ApiResponse<TransferResponse> receive(@PathVariable Long id) {
        return ApiResponse.ok(service.receive(id));
    }
}
