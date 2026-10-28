package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.SerialStatus;

import java.time.LocalDate;

public record SerialResponse(
        Long id,
        String serialNumber,
        Long productId,
        String sku,
        String productName,
        SerialStatus status,
        String warehouseName,
        String locationCode,
        String lotNumber,
        LocalDate expiryDate,
        LocalDate warrantyUntil
) {}
