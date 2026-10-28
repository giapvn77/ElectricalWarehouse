package com.electricalwarehouse.dto.response;

public record ThresholdResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        Long productId,
        String sku,
        String productName,
        int minQuantity,
        int maxQuantity
) {}
