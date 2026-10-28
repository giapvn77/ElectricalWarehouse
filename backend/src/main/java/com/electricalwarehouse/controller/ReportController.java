package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.ReportFilterRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.InventoryReportResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockTransactionResponse;
import com.electricalwarehouse.dto.response.XNTReportResponse;
import com.electricalwarehouse.integration.ExportFormat;
import com.electricalwarehouse.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<InventoryReportResponse> inventory(@RequestParam(required = false) Long warehouseId) {
        return ApiResponse.ok(reportService.inventoryReport(warehouseId));
    }

    @GetMapping("/movement")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<PagedResult<StockTransactionResponse>> movement(
            @ModelAttribute ReportFilterRequest filter,
            @PageableDefault(size = 50, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(reportService.movementReport(filter, pageable));
    }

    @GetMapping("/xnt")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ApiResponse<XNTReportResponse> xnt(@ModelAttribute ReportFilterRequest filter) {
        return ApiResponse.ok(reportService.xntReport(filter));
    }

    @GetMapping("/inventory/export")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<byte[]> exportInventory(@RequestParam(required = false) Long warehouseId,
                                                  @RequestParam(defaultValue = "EXCEL") ExportFormat format) {
        return file("ton-kho", format, reportService.exportInventory(warehouseId, format));
    }

    @GetMapping("/movement/export")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<byte[]> exportMovement(@ModelAttribute ReportFilterRequest filter,
                                                 @RequestParam(defaultValue = "EXCEL") ExportFormat format) {
        return file("bien-dong-kho", format, reportService.exportMovement(filter, format));
    }

    @GetMapping("/xnt/export")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<byte[]> exportXnt(@ModelAttribute ReportFilterRequest filter,
                                            @RequestParam(defaultValue = "EXCEL") ExportFormat format) {
        return file("nhap-xuat-ton", format, reportService.exportXnt(filter, format));
    }

    private static ResponseEntity<byte[]> file(String name, ExportFormat format, byte[] content) {
        String fileName = name + "-" + LocalDate.now() + "." + format.extension();
        return ResponseEntity.ok()
                .contentType(format.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .body(content);
    }
}
