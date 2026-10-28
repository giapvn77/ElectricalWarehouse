package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions",
        indexes = {
                @Index(name = "ix_tx_wh_product_date", columnList = "warehouse_id, product_id, transaction_date"),
                @Index(name = "ix_tx_reference", columnList = "reference_type, reference_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class StockTransaction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

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

    /** Số lượng có dấu: dương = tăng tồn, âm = giảm tồn. */
    @Column(nullable = false)
    private int quantity;

    /** Tồn của sản phẩm trong kho sau giao dịch. */
    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 20)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_code", length = 30)
    private String referenceCode;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
}
