package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.CreateStockOutRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockOutResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Pageable;

/**
 * Phiếu xuất: DRAFT -> PENDING -> APPROVED (giữ hàng) -> PICKING (lấy hàng) -> COMPLETED (trừ tồn).
 */
public interface StockOutService {

    PagedResult<StockOutResponse> search(ReceiptStatus status, Long warehouseId, String keyword, Pageable pageable);

    StockOutResponse get(Long id);

    StockOutResponse create(CreateStockOutRequest request);

    StockOutResponse update(Long id, CreateStockOutRequest request);

    StockOutResponse submit(Long id);

    StockOutResponse approve(Long id);

    StockOutResponse reject(Long id, RejectRequest request);

    StockOutResponse cancel(Long id);

    StockOutResponse complete(Long id);
}
