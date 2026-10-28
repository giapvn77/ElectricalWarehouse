package com.electricalwarehouse.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockInDetailResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        boolean serialManaged,
        Long locationId,
        String locationCode,
        int quantity,
        int scannedQuantity,
        BigDecimal unitPrice,
        String lotNumber,
        LocalDate expiryDate
) {}
