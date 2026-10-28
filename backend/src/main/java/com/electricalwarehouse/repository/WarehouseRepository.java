package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.WarehouseType;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCode(String code);

    List<Warehouse> findByActiveTrue();

    Optional<Warehouse> findFirstByTypeAndActiveTrue(WarehouseType type);
}
