package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    @Query("""
            select r from StockTransfer r
            where (:status is null or r.status = :status)
              and (:warehouseId is null or r.fromWarehouse.id = :warehouseId)
              and (:keyword is null or lower(r.code) like lower(concat('%', :keyword, '%')))
            """)
    Page<StockTransfer> search(@Param("status") ReceiptStatus status, @Param("warehouseId") Long warehouseId,
                       @Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ReceiptStatus status);

    List<StockTransfer> findTop20ByStatusInOrderByCreatedAtAsc(Collection<ReceiptStatus> statuses);
}
