package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;

import java.time.LocalDateTime;
import java.util.List;

/** Every document waiting for a decision, merged into one list for the dashboard. */
public record DashboardPendingApprovalResponse(
        long stockInCount,
        long stockOutCount,
        long transferCount,
        long adjustmentCount,
        long total,
        List<Item> items
) {
    public record Item(ReferenceType type, Long id, String code, String warehouseName, String createdByName,
                       ReceiptStatus status, LocalDateTime createdAt) {
    }
}
