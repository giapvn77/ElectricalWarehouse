package com.electricalwarehouse.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String username,
        String action,
        String entityType,
        Long entityId,
        String detail,
        String ipAddress,
        LocalDateTime createdAt
) {}
