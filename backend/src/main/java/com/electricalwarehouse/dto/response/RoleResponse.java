package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.RoleCode;

import java.util.Set;

public record RoleResponse(
        Long id,
        RoleCode code,
        String name,
        String description,
        Set<String> permissions
) {}
