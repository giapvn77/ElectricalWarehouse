package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_categories")
@Getter
@Setter
@NoArgsConstructor
public class ProductCategory extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, columnDefinition = "NVARCHAR(150)")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProductCategory parent;

    @Column(nullable = false)
    private boolean active = true;
}
