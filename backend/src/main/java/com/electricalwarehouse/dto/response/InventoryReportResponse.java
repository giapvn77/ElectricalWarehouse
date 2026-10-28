package com.electricalwarehouse.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record InventoryReportResponse(
        Long warehouseId,
        String warehouseName,
        List<Row> rows,
        long totalQuantity,
        BigDecimal totalValue
) {
    public record Row(String warehouseName, String sku, String productName, String unitName, long quantity,
                      Integer minQuantity, Integer maxQuantity, BigDecimal costPrice, BigDecimal value) {
    }
}
