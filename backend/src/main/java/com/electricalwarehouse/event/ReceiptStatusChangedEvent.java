package com.electricalwarehouse.event;

import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;

/** Published when an approval document moves to another status. */
public record ReceiptStatusChangedEvent(ReferenceType referenceType, Long documentId, String documentCode,
                                        Long warehouseId, ReceiptStatus oldStatus, ReceiptStatus newStatus,
                                        Long actorId, Long creatorId, String reason) {
}
