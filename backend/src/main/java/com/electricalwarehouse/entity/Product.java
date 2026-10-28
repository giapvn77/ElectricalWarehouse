package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = @Index(name = "ix_product_barcode", columnList = "barcode"))
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, columnDefinition = "NVARCHAR(200)")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String brand;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String specification;

    /** Unique when present; enforced in ProductService because SQL Server UNIQUE allows only one NULL. */
    @Column(length = 100)
    private String barcode;

    /** Mỗi đơn vị hàng có một số serial riêng (bắt buộc quét khi nhập/xuất). */
    @Column(name = "serial_managed", nullable = false)
    private boolean serialManaged = false;

    /** Quản lý hạn sử dụng: xuất theo FEFO thay vì FIFO. */
    @Column(name = "expiry_managed", nullable = false)
    private boolean expiryManaged = false;

    @Column(name = "warranty_months")
    private Integer warrantyMonths;

    @Column(name = "cost_price", precision = 18, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "sale_price", precision = 18, scale = 2)
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}
