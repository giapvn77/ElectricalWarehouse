package com.electricalwarehouse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_locations", uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
public class StorageLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    /** Mã vị trí, ví dụ A-01-02 (khu - kệ - tầng). */
    @Column(nullable = false, length = 30)
    private String code;

    @Column(length = 20)
    private String zone;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
