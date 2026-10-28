package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_out_serial_allocations", uniqueConstraints = @UniqueConstraint(columnNames = {"detail_id", "serial_id"}))
@Getter
@Setter
@NoArgsConstructor
public class StockOutSerialAllocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detail_id")
    private StockOutReceiptDetail detail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serial_id")
    private ProductSerial serial;

    @Column(name = "picked_at", nullable = false)
    private LocalDateTime pickedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picked_by")
    private User pickedBy;
}
