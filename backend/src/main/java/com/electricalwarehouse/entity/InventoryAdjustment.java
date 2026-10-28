package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_adjustments")
@Getter
@Setter
@NoArgsConstructor
public class InventoryAdjustment extends ApprovableDocument {

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

    /** Tồn trên hệ thống tại thời điểm kiểm kê. */
    @Column(name = "system_quantity", nullable = false)
    private int systemQuantity;

    /** Tồn thực tế đếm được. */
    @Column(name = "actual_quantity", nullable = false)
    private int actualQuantity;

    @Column(nullable = false, columnDefinition = "NVARCHAR(500)")
    private String reason;

    public int getDifference() {
        return actualQuantity - systemQuantity;
    }
}
