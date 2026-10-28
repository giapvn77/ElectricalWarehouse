package com.electricalwarehouse.dto.response;

import java.time.LocalDate;
import java.util.List;

public record XNTReportResponse(
        LocalDate fromDate,
        LocalDate toDate,
        Long warehouseId,
        String warehouseName,
        List<Row> rows
) {
    /** Nhập - Xuất - Tồn của một sản phẩm: closing = opening + in - out. */
    public record Row(Long productId, String sku, String productName, String unitName, long openingQuantity,
                      long inQuantity, long outQuantity, long closingQuantity) {
    }
}
