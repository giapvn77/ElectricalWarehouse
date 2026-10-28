package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.DefectiveGood;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.DefectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DefectiveGoodRepository extends JpaRepository<DefectiveGood, Long> {
    @Query("""
            select d from DefectiveGood d
            where (:status is null or d.status = :status)
              and (:warehouseId is null or d.sourceWarehouse.id = :warehouseId)
            """)
    Page<DefectiveGood> search(@Param("status") DefectStatus status, @Param("warehouseId") Long warehouseId,
                               Pageable pageable);
}
