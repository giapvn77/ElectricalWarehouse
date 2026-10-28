package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockOutReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockOutReceiptRepository extends JpaRepository<StockOutReceipt, Long> {
    @Query("""
            select r from StockOutReceipt r
            where (:status is null or r.status = :status)
              and (:warehouseId is null or r.warehouse.id = :warehouseId)
              and (:keyword is null or lower(r.code) like lower(concat('%', :keyword, '%')))
            """)
    Page<StockOutReceipt> search(@Param("status") ReceiptStatus status, @Param("warehouseId") Long warehouseId,
                       @Param("keyword") String keyword, Pageable pageable);

    long countByStatus(ReceiptStatus status);

    List<StockOutReceipt> findTop20ByStatusInOrderByCreatedAtAsc(Collection<ReceiptStatus> statuses);
}
