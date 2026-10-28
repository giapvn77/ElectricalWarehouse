package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RolePermissionRequest(
        @NotNull Set<String> permissionCodes
) {}
