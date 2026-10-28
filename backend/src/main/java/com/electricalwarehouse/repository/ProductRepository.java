package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsBySku(String sku);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    @Query("""
            select p from Product p
            where (:categoryId is null or p.category.id = :categoryId)
              and (:keyword is null
                   or lower(p.sku) like lower(concat('%', :keyword, '%'))
                   or lower(p.name) like lower(concat('%', :keyword, '%'))
                   or p.barcode = :keyword)
            """)
    Page<Product> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);
}
