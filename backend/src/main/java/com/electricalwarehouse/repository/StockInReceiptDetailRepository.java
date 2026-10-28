package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockInReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockInReceiptDetailRepository extends JpaRepository<StockInReceiptDetail, Long> {
}
