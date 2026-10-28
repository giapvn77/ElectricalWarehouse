package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_transfer_details")
@Getter
@Setter
@NoArgsConstructor
public class StockTransferDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id")
    private StockTransfer transfer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private StorageLocation toLocation;

    @Column(nullable = false)
    private int quantity;

    /** Danh sách serial chuyển kho, phân tách bởi dấu phẩy (với hàng quản lý serial). */
    @Column(name = "serial_numbers", columnDefinition = "NVARCHAR(MAX)")
    private String serialNumbers;
}
