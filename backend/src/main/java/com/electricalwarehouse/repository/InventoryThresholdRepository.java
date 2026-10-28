package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.InventoryThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryThresholdRepository extends JpaRepository<InventoryThreshold, Long> {
    Optional<InventoryThreshold> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    List<InventoryThreshold> findByWarehouseId(Long warehouseId);
}
