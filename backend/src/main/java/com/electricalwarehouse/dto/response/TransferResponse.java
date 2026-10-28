package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.ReceiptStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TransferResponse(
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
        Long fromWarehouseId,
        String fromWarehouseName,
        Long toWarehouseId,
        String toWarehouseName,
        LocalDateTime receivedAt,
        String receivedByName,
        List<TransferDetailResponse> details
) {}
