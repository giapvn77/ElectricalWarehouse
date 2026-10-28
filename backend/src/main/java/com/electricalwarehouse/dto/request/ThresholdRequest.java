package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ThresholdRequest(
        @NotNull Long warehouseId,
        @NotNull Long productId,
        @Min(0) int minQuantity,
        @Min(0) int maxQuantity
) {}
