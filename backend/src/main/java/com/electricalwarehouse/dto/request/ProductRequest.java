package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 50) String sku,
        @NotBlank @Size(max = 200) String name,
        Long categoryId,
        Long unitId,
        String brand,
        String specification,
        String barcode,
        boolean serialManaged,
        boolean expiryManaged,
        Integer warrantyMonths,
        @PositiveOrZero BigDecimal costPrice,
        @PositiveOrZero BigDecimal salePrice,
        Boolean active
) {}
