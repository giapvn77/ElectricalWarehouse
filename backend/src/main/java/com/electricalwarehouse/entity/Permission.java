package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
public class Permission extends BaseEntity {
    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(length = 50)
    private String module;
}
