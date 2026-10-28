package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.OutType;
import com.electricalwarehouse.entity.enums.ReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StockOutResponse(
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
        Long customerId,
        String customerName,
        OutType outType,
        LocalDate receiptDate,
        BigDecimal totalAmount,
        String pickedByName,
        List<StockOutDetailResponse> details
) {}
