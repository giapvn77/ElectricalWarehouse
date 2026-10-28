package com.electricalwarehouse.dto.request;

import com.electricalwarehouse.entity.enums.DefectSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DefectRegistrationRequest(
        @NotNull Long productId,
        @NotNull Long warehouseId,
        String serialNumber,
        @Positive int quantity,
        @NotNull DefectSource source,
        @NotBlank @Size(max = 1000) String description,
        Long customerId,
        Long supplierId
) {}
