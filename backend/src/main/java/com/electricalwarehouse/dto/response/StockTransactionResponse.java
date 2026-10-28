package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.TransactionType;

import java.time.LocalDateTime;

public record StockTransactionResponse(
        Long id,
        TransactionType type,
        String warehouseName,
        String sku,
        String productName,
        String locationCode,
        String lotNumber,
        int quantity,
        int balanceAfter,
        ReferenceType referenceType,
        String referenceCode,
        String note,
        String performedByName,
        LocalDateTime transactionDate
) {}
