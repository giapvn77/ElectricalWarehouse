package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.RejectRequest;
import com.electricalwarehouse.dto.request.TransferRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.TransferResponse;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import org.springframework.data.domain.Pageable;

/**
 * Chuyển kho: DRAFT -> PENDING -> APPROVED (xuất khỏi kho nguồn, hàng đang đi đường)
 * -> COMPLETED (kho đích xác nhận nhận hàng).
 */
public interface StockTransferService {

    PagedResult<TransferResponse> search(ReceiptStatus status, Long warehouseId, String keyword, Pageable pageable);

    TransferResponse get(Long id);

    TransferResponse create(TransferRequest request);

    TransferResponse submit(Long id);

    TransferResponse approve(Long id);

    TransferResponse reject(Long id, RejectRequest request);

    TransferResponse cancel(Long id);

    TransferResponse receive(Long id);
}
