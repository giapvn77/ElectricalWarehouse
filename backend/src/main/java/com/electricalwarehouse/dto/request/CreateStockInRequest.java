package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateStockInRequest(
        @NotNull Long warehouseId,
        Long supplierId,
        LocalDate receiptDate,
        String invoiceNumber,
        String note,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull Long productId,
            Long locationId,
            @Positive int quantity,
            @PositiveOrZero BigDecimal unitPrice,
            String lotNumber,
            LocalDate expiryDate
    ) {
    }
}
