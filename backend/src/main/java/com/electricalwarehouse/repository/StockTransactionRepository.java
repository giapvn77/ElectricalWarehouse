package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    @Query("""
            select t from StockTransaction t
            where (:warehouseId is null or t.warehouse.id = :warehouseId)
              and (:productId is null or t.product.id = :productId)
              and (:type is null or t.type = :type)
              and t.transactionDate >= :from and t.transactionDate < :to
            """)
    Page<StockTransaction> search(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
                                  @Param("type") TransactionType type, @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to, Pageable pageable);

    /**
     * Nhập - Xuất - Tồn theo sản phẩm trong khoảng [from, to).
     * Tồn đầu = tổng giao dịch trước from; nhập/xuất = tổng dương/âm trong kỳ.
     */
    @Query("""
            select t.product.id as productId,
                   sum(case when t.transactionDate < :from then t.quantity else 0 end) as openingQty,
                   sum(case when t.transactionDate >= :from and t.quantity > 0 then t.quantity else 0 end) as inQty,
                   sum(case when t.transactionDate >= :from and t.quantity < 0 then -t.quantity else 0 end) as outQty
            from StockTransaction t
            where (:warehouseId is null or t.warehouse.id = :warehouseId)
              and t.transactionDate < :to
            group by t.product.id
            """)
    List<XntView> aggregateXnt(@Param("warehouseId") Long warehouseId, @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    interface XntView {
        Long getProductId();

        Long getOpeningQty();

        Long getInQty();

        Long getOutQty();
    }
}
