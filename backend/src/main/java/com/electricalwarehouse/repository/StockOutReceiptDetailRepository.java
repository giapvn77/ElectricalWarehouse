package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockOutReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOutReceiptDetailRepository extends JpaRepository<StockOutReceiptDetail, Long> {
}
