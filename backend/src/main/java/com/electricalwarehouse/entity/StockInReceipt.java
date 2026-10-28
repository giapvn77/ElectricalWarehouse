package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_in_receipts")
@Getter
@Setter
@NoArgsConstructor
public class StockInReceipt extends ApprovableDocument {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate = LocalDate.now();

    /** Số hóa đơn / chứng từ của nhà cung cấp. */
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockInReceiptDetail> details = new ArrayList<>();

    public void addDetail(StockInReceiptDetail detail) {
        detail.setReceipt(this);
        details.add(detail);
    }
}
