package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.CreateStockInRequest;
import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockInResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Pageable;

/**
 * Phiếu nhập: DRAFT -> PENDING -> APPROVED -> (quét serial) -> COMPLETED.
 * Inventory is only posted on completion.
 */
public interface StockInService {

    PagedResult<StockInResponse> search(ReceiptStatus status, Long warehouseId, String keyword, Pageable pageable);

    StockInResponse get(Long id);

    StockInResponse create(CreateStockInRequest request);

    StockInResponse update(Long id, CreateStockInRequest request);

    StockInResponse submit(Long id);

    StockInResponse approve(Long id);

    StockInResponse reject(Long id, RejectRequest request);

    StockInResponse cancel(Long id);

    StockInResponse complete(Long id);
}
