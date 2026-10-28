package com.electricalwarehouse.dto.response;

public record StorageLocationResponse(
        Long id,
        Long warehouseId,
        String code,
        String zone,
        String description,
        boolean active
) {}
