package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockInReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockInReceiptRepository extends JpaRepository<StockInReceipt, Long> {
    @Query("""
            select r from StockInReceipt r
            where (:status is null or r.status = :status)
              and (:warehouseId is null or r.warehouse.id = :warehouseId)
              and (:keyword is null or lower(r.code) like lower(concat('%', :keyword, '%')))
            """)
    Page<StockInReceipt> search(@Param("status") ReceiptStatus status, @Param("warehouseId") Long warehouseId,
                       @Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ReceiptStatus status);

    List<StockInReceipt> findTop20ByStatusInOrderByCreatedAtAsc(Collection<ReceiptStatus> statuses);
}
