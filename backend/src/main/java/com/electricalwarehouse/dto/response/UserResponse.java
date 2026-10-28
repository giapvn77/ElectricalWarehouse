package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.RoleCode;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        boolean active,
        Long warehouseId,
        String warehouseName,
        Set<RoleCode> roles,
        Set<String> permissions
) {}
