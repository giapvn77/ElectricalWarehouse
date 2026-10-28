package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {
    @Query("""
            select r from InventoryAdjustment r
            where (:status is null or r.status = :status)
              and (:warehouseId is null or r.warehouse.id = :warehouseId)
              and (:keyword is null or lower(r.code) like lower(concat('%', :keyword, '%')))
            """)
    Page<InventoryAdjustment> search(@Param("status") ReceiptStatus status, @Param("warehouseId") Long warehouseId,
                       @Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ReceiptStatus status);

    List<InventoryAdjustment> findTop20ByStatusInOrderByCreatedAtAsc(Collection<ReceiptStatus> statuses);
}
