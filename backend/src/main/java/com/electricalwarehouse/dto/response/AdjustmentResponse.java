package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.ReceiptStatus;

import java.time.LocalDateTime;

public record AdjustmentResponse(
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
        Long productId,
        String sku,
        String productName,
        String locationCode,
        String lotNumber,
        int systemQuantity,
        int actualQuantity,
        int difference,
        String reason
) {}
