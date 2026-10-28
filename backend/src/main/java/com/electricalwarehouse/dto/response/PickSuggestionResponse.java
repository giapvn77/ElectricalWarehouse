package com.electricalwarehouse.dto.response;

import java.time.LocalDate;
import java.util.List;

/** Pick list for one stock-out line, ordered by FIFO/FEFO. */
public record PickSuggestionResponse(
        Long detailId,
        Long productId,
        String sku,
        String productName,
        boolean serialManaged,
        int remainingQuantity,
        List<PickLocation> locations
) {
    public record PickLocation(Long inventoryId, Long locationId, String locationCode, String lotNumber,
                               LocalDate expiryDate, int availableQuantity, int suggestedQuantity) {
    }
}
