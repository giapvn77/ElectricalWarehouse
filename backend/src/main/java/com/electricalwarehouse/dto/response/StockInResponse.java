package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.ReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StockInResponse(
        Long id,
        String code,
        ReceiptStatus status,
        String note,
        Long createdById,
        String createdByName,
        String approvedByName,
        LocalDateTime approvedAt,
        String rejectReason,
        LocalDateTime createdAt,
        Long warehouseId,
        String warehouseName,
        Long supplierId,
        String supplierName,
        LocalDate receiptDate,
        String invoiceNumber,
        BigDecimal totalAmount,
        List<StockInDetailResponse> details
) {}
