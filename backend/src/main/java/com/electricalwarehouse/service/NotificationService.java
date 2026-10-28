package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.response.NotificationResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.enums.NotificationType;
import com.electricalwarehouse.entity.enums.ReferenceType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void notify(User recipient, NotificationType type, String title, String message, ReferenceType refType, Long refId);

    /** Skips the notification if the same recipient got the same alert in the last 24 hours. */
    void notifyOnce(User recipient, NotificationType type, String title, String message, Long refId);

    PagedResult<NotificationResponse> getMine(Pageable pageable);

    long countUnread();

    void markRead(Long id);

    void markAllRead();
}
