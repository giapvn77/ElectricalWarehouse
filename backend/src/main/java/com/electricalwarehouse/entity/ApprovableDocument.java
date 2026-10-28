package com.electricalwarehouse.entity;

import com.electricalwarehouse.entity.enums.ReceiptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Shared columns of every document that goes through the approval workflow. */
@MappedSuperclass
@Getter
@Setter
public abstract class ApprovableDocument extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReceiptStatus status = ReceiptStatus.DRAFT;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reject_reason", columnDefinition = "NVARCHAR(500)")
    private String rejectReason;

    @Version
    private Long version;
}
