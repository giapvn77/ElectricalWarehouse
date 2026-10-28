package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    boolean existsByCode(String code);
}
