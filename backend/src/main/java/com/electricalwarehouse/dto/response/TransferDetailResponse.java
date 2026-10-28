package com.electricalwarehouse.dto.response;

public record TransferDetailResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        int quantity,
        Long toLocationId,
        String toLocationCode,
        String serialNumbers
) {}
