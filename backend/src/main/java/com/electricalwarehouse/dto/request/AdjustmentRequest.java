package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdjustmentRequest(
        @NotNull Long warehouseId,
        @NotNull Long productId,
        Long locationId,
        String lotNumber,
        @Min(0) int actualQuantity,
        @NotBlank @Size(max = 500) String reason
) {}
