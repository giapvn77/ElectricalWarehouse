package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.electricalwarehouse.entity.enums.WarehouseType;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(300)")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WarehouseType type = WarehouseType.MAIN;

    /** Email nhận thông báo duyệt phiếu của kho. */
    @Column(name = "manager_email", length = 150)
    private String managerEmail;

    @Column(nullable = false)
    private boolean active = true;
}
