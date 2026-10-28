package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.DefectSource;
import com.electricalwarehouse.entity.enums.DefectStatus;

import java.time.LocalDateTime;

public record DefectiveGoodResponse(
        Long id,
        String code,
        Long productId,
        String sku,
        String productName,
        String serialNumber,
        String sourceWarehouseName,
        String defectiveWarehouseName,
        String customerName,
        String supplierName,
        int quantity,
        DefectSource source,
        DefectStatus status,
        String description,
        String reportedByName,
        String resolutionNote,
        LocalDateTime resolvedAt,
        LocalDateTime createdAt
) {}
