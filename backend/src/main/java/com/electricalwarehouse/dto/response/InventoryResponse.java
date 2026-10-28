package com.electricalwarehouse.dto.response;

import java.time.LocalDate;

public record InventoryResponse(
        Long id,
        Long warehouseId,
        String warehouseName,
        Long productId,
        String sku,
        String productName,
        String unitName,
        Long locationId,
        String locationCode,
        String lotNumber,
        LocalDate expiryDate,
        int quantity,
        int reservedQuantity,
        int availableQuantity
) {}
