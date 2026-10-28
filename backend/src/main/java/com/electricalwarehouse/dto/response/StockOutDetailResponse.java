package com.electricalwarehouse.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record StockOutDetailResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        boolean serialManaged,
        int quantity,
        int pickedQuantity,
        BigDecimal unitPrice,
        List<String> serialNumbers
) {}
