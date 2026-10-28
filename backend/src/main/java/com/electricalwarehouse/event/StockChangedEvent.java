package com.electricalwarehouse.event;

import com.electricalwarehouse.entity.enums.TransactionType;

/** Published after inventory of a product in a warehouse changes. */
public record StockChangedEvent(Long warehouseId, Long productId, int delta, int balanceAfter,
                                TransactionType type) {
}
