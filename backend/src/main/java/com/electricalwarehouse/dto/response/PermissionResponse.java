package com.electricalwarehouse.dto.response;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String module
) {}
