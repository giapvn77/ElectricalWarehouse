package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalse(Long recipientId);

    boolean existsByRecipientIdAndTypeAndReferenceIdAndCreatedAtAfter(
            Long recipientId, com.electricalwarehouse.entity.enums.NotificationType type, Long referenceId,
            LocalDateTime after);

    @Modifying
    @Query("update Notification n set n.read = true where n.recipient.id = :userId and n.read = false")
    int markAllRead(@Param("userId") Long userId);
}
