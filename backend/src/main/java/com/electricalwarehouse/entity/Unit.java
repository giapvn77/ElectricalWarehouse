package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
public class Unit extends BaseEntity {
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
    private String name;
}
