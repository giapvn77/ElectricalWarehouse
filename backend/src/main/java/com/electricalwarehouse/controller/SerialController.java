package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.ScanSerialRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.SerialResponse;
import com.electricalwarehouse.integration.BarcodeService;
import com.electricalwarehouse.service.SerialScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Quét và tra cứu số serial. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SerialController {
    private final SerialScanService serialScanService;
    private final BarcodeService barcodeService;

    @PostMapping("/stock-in/{receiptId}/serials")
    @PreAuthorize("hasAuthority('STOCK_IN_CREATE')")
    public ApiResponse<List<SerialResponse>> scan(@PathVariable Long receiptId,
                                                  @Valid @RequestBody ScanSerialRequest request) {
        return ApiResponse.ok(serialScanService.scan(receiptId, request));
    }

    @DeleteMapping("/stock-in/{receiptId}/serials/{serialId}")
    @PreAuthorize("hasAuthority('STOCK_IN_CREATE')")
    public ApiResponse<Void> remove(@PathVariable Long receiptId, @PathVariable Long serialId) {
        serialScanService.remove(receiptId, serialId);
        return ApiResponse.ok();
    }

    @GetMapping("/stock-in/details/{detailId}/serials")
    public ApiResponse<List<SerialResponse>> byDetail(@PathVariable Long detailId) {
        return ApiResponse.ok(serialScanService.getByReceiptDetail(detailId));
    }

    @GetMapping("/serials/{serialNumber}")
    public ApiResponse<SerialResponse> lookup(@PathVariable String serialNumber) {
        return ApiResponse.ok(serialScanService.lookup(serialNumber));
    }

    @GetMapping(value = "/serials/{serialNumber}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@PathVariable String serialNumber) {
        SerialResponse serial = serialScanService.lookup(serialNumber);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .body(barcodeService.qrCode(serial.serialNumber(), 200));
    }
}
