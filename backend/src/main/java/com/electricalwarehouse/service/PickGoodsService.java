package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.PickGoodsRequest;
import com.electricalwarehouse.dto.response.PickSuggestionResponse;
import com.electricalwarehouse.dto.response.StockOutResponse;

import java.util.List;

/** Warehouse staff picking goods for an approved stock-out receipt. */
public interface PickGoodsService {

    /** Suggested locations per line, FIFO/FEFO ordered. */
    List<PickSuggestionResponse> getPickList(Long receiptId);

    StockOutResponse pick(Long receiptId, PickGoodsRequest request);
}
