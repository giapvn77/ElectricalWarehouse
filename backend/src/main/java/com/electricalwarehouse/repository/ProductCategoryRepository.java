package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    boolean existsByCode(String code);
}
