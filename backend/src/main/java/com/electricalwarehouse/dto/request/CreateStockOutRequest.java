package com.electricalwarehouse.dto.request;

import com.electricalwarehouse.entity.enums.OutType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateStockOutRequest(
        @NotNull Long warehouseId,
        Long customerId,
        @NotNull OutType outType,
        LocalDate receiptDate,
        String note,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull Long productId,
            @Positive int quantity,
            @PositiveOrZero BigDecimal unitPrice
    ) {
    }
}
