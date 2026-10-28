package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockOutSerialAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockOutSerialAllocationRepository extends JpaRepository<StockOutSerialAllocation, Long> {
    List<StockOutSerialAllocation> findByDetailReceiptId(Long receiptId);
}
