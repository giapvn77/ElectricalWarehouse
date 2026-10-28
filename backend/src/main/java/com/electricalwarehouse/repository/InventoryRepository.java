package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    /** Derived query: null arguments become IS NULL, so rows without location/lot match too. */
    Optional<Inventory> findByWarehouseIdAndProductIdAndLocationIdAndLotNumber(
            Long warehouseId, Long productId, Long locationId, String lotNumber);

    List<Inventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    @Query("select coalesce(sum(i.quantity), 0) from Inventory i where i.warehouse.id = :wh and i.product.id = :product")
    int sumQuantity(@Param("wh") Long warehouseId, @Param("product") Long productId);

    @Query("""
            select coalesce(sum(i.quantity - i.reservedQuantity), 0) from Inventory i
            where i.warehouse.id = :wh and i.product.id = :product
            """)
    int sumAvailable(@Param("wh") Long warehouseId, @Param("product") Long productId);

    @Query("""
            select i from Inventory i
            where i.quantity > 0
              and (:warehouseId is null or i.warehouse.id = :warehouseId)
              and (:keyword is null
                   or lower(i.product.sku) like lower(concat('%', :keyword, '%'))
                   or lower(i.product.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<Inventory> search(@Param("warehouseId") Long warehouseId, @Param("keyword") String keyword, Pageable pageable);

    /** Tổng tồn theo kho + sản phẩm (dùng cho báo cáo và cảnh báo Min-Max). */
    @Query("""
            select i.warehouse.id as warehouseId, i.product.id as productId, sum(i.quantity) as quantity
            from Inventory i
            where (:warehouseId is null or i.warehouse.id = :warehouseId)
            group by i.warehouse.id, i.product.id
            """)
    List<StockSummaryView> summarize(@Param("warehouseId") Long warehouseId);

    @Query("""
            select i from Inventory i
            where i.quantity > 0 and i.expiryDate is not null and i.expiryDate <= :date
            """)
    List<Inventory> findExpiringBefore(@Param("date") java.time.LocalDate date);

    interface StockSummaryView {
        Long getWarehouseId();

        Long getProductId();

        Long getQuantity();
    }
}
