package com.electricalwarehouse.dto.response;

import com.electricalwarehouse.entity.enums.NotificationType;
import com.electricalwarehouse.entity.enums.ReferenceType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        ReferenceType referenceType,
        Long referenceId,
        LocalDateTime createdAt
) {}
