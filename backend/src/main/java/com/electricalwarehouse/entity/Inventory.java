package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventories",
        indexes = @Index(name = "ix_inventory_wh_product", columnList = "warehouse_id, product_id"))
@Getter
@Setter
@NoArgsConstructor
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private StorageLocation location;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** Ngày nhập lô hàng, dùng để sắp xếp FIFO. */
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Column(nullable = false)
    private int quantity = 0;

    /** Số lượng đã giữ chỗ cho phiếu xuất đã duyệt nhưng chưa hoàn tất. */
    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity = 0;

    @Version
    private Long version;

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}
