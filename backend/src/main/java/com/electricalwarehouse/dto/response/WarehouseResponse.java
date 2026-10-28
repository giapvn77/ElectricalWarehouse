package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.WarehouseType;

public record WarehouseResponse(
        Long id,
        String code,
        String name,
        String address,
        WarehouseType type,
        String managerEmail,
        boolean active
) {}
