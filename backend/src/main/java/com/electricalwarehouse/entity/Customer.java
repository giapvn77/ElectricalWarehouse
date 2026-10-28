package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends BaseEntity {
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "NVARCHAR(300)")
    private String address;

    @Column(name = "contact_person", columnDefinition = "NVARCHAR(150)")
    private String contactPerson;

    @Column(nullable = false)
    private boolean active = true;
}
