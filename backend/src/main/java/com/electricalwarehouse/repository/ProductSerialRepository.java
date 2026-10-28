package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.ProductSerial;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.SerialStatus;

import java.util.List;
import java.util.Optional;

public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    Optional<ProductSerial> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    List<ProductSerial> findByStockInDetailId(Long stockInDetailId);

    List<ProductSerial> findByWarehouseIdAndProductIdAndStatus(Long warehouseId, Long productId, SerialStatus status);
}
