package com.electricalwarehouse.dto.response;

public record CategoryResponse(
        Long id,
        String code,
        String name,
        Long parentId,
        String parentName,
        boolean active
) {}
