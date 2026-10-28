package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.electricalwarehouse.entity.enums.SerialStatus;

import java.time.LocalDate;

@Entity
@Table(name = "product_serials")
@Getter
@Setter
@NoArgsConstructor
public class ProductSerial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private StorageLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_in_detail_id")
    private StockInReceiptDetail stockInDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SerialStatus status = SerialStatus.IN_STOCK;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "warranty_until")
    private LocalDate warrantyUntil;
}
