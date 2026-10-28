package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.electricalwarehouse.entity.enums.OutType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_out_receipts")
@Getter
@Setter
@NoArgsConstructor
public class StockOutReceipt extends ApprovableDocument {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "out_type", nullable = false, length = 20)
    private OutType outType = OutType.SALE;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate = LocalDate.now();

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picked_by")
    private User pickedBy;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockOutReceiptDetail> details = new ArrayList<>();

    public void addDetail(StockOutReceiptDetail detail) {
        detail.setReceipt(this);
        details.add(detail);
    }
}
