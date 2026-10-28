package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.StockTransferDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransferDetailRepository extends JpaRepository<StockTransferDetail, Long> {
}
