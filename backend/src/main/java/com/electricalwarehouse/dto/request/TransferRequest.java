package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;

import java.util.List;

public record TransferRequest(
        @NotNull Long fromWarehouseId,
        @NotNull Long toWarehouseId,
        String note,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull Long productId,
            @Positive int quantity,
            Long toLocationId,
            List<String> serialNumbers
    ) {
    }
}
