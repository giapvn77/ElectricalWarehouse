package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.PickGoodsRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PickSuggestionResponse;
import com.electricalwarehouse.dto.response.StockOutResponse;
import com.electricalwarehouse.service.PickGoodsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Lấy hàng theo gợi ý FIFO/FEFO. */
@RestController
@RequestMapping("/api/stock-out/{receiptId}")
@RequiredArgsConstructor
public class PickGoodsController {
    private final PickGoodsService pickGoodsService;

    @GetMapping("/pick-list")
    @PreAuthorize("hasAuthority('STOCK_OUT_PICK')")
    public ApiResponse<List<PickSuggestionResponse>> pickList(@PathVariable Long receiptId) {
        return ApiResponse.ok(pickGoodsService.getPickList(receiptId));
    }

    @PostMapping("/pick")
    @PreAuthorize("hasAuthority('STOCK_OUT_PICK')")
    public ApiResponse<StockOutResponse> pick(@PathVariable Long receiptId, @Valid @RequestBody PickGoodsRequest request) {
        return ApiResponse.ok(pickGoodsService.pick(receiptId, request));
    }
}
