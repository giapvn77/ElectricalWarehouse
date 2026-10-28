package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.response.NotificationResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.entity.Notification;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.enums.NotificationType;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.NotificationRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DTOMapper mapper;

    @Override
    @Transactional
    public void notify(User recipient, NotificationType type, String title, String message, ReferenceType refType,
                       Long refId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceType(refType);
        n.setReferenceId(refId);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void notifyOnce(User recipient, NotificationType type, String title, String message, Long refId) {
        boolean recent = notificationRepository.existsByRecipientIdAndTypeAndReferenceIdAndCreatedAtAfter(
                recipient.getId(), type, refId, LocalDateTime.now().minusHours(24));
        if (!recent) {
            notify(recipient, type, title, message, null, refId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<NotificationResponse> getMine(Pageable pageable) {
        return PagedResult.of(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(CurrentUser.id(), pageable),
                mapper::toNotificationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByRecipientIdAndReadFalse(CurrentUser.id());
    }

    @Override
    @Transactional
    public void markRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .filter(x -> x.getRecipient().getId().equals(CurrentUser.id()))
                .orElseThrow(() -> new NotFoundException("Thông báo", id));
        n.setRead(true);
    }

    @Override
    @Transactional
    public void markAllRead() {
        notificationRepository.markAllRead(CurrentUser.id());
    }
}
