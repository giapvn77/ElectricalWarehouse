package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.ReportFilterRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockTransactionResponse;
import com.electricalwarehouse.entity.StockTransaction;
import com.electricalwarehouse.entity.StorageLocation;
import org.springframework.data.domain.Pageable;

/** Append-only ledger of every stock change (thẻ kho). */
public interface StockTransactionService {

    StockTransaction record(StockMovement movement, StorageLocation location, String lotNumber, int signedQuantity,
                            int balanceAfter);

    PagedResult<StockTransactionResponse> search(ReportFilterRequest filter, Pageable pageable);
}
