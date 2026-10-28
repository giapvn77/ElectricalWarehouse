package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.electricalwarehouse.entity.enums.DefectSource;
import com.electricalwarehouse.entity.enums.DefectStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "defective_goods")
@Getter
@Setter
@NoArgsConstructor
public class DefectiveGood extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serial_id")
    private ProductSerial serial;

    /** Kho phát sinh hàng lỗi. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_warehouse_id")
    private Warehouse sourceWarehouse;

    /** Kho hàng lỗi đang giữ hàng. */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defective_warehouse_id")
    private Warehouse defectiveWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private int quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DefectSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DefectStatus status = DefectStatus.REPORTED;

    @Column(nullable = false, columnDefinition = "NVARCHAR(1000)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @Column(name = "resolution_note", columnDefinition = "NVARCHAR(1000)")
    private String resolutionNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
