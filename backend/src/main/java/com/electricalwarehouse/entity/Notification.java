package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.electricalwarehouse.entity.enums.NotificationType;
import com.electricalwarehouse.entity.enums.ReferenceType;

@Entity
@Table(name = "notifications",
        indexes = @Index(name = "ix_notification_recipient", columnList = "recipient_id, is_read"))
@Getter
@Setter
@NoArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "NVARCHAR(200)")
    private String title;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 20)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;
}
