package com.electricalwarehouse.dto.request;

import com.electricalwarehouse.entity.enums.TransactionType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReportFilterRequest(
        Long warehouseId,
        Long productId,
        TransactionType type,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
) {
    /** Defaults to the current month when dates are missing. */
    public LocalDate fromOrDefault() {
        return fromDate != null ? fromDate : LocalDate.now().withDayOfMonth(1);
    }

    public LocalDate toOrDefault() {
        return toDate != null ? toDate : LocalDate.now();
    }
}
