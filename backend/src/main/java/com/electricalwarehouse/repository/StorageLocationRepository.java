package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StorageLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {
    List<StorageLocation> findByWarehouseIdOrderByCode(Long warehouseId);

    boolean existsByWarehouseIdAndCode(Long warehouseId, String code);
}
