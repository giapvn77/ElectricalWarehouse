package com.electricalwarehouse.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        Long categoryId,
        String categoryName,
        Long unitId,
        String unitName,
        String brand,
        String specification,
        String barcode,
        boolean serialManaged,
        boolean expiryManaged,
        Integer warrantyMonths,
        BigDecimal costPrice,
        BigDecimal salePrice,
        boolean active,
        List<String> imageUrls
) {}
