package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.ScanSerialRequest;
import com.electricalwarehouse.dto.response.SerialResponse;

import java.util.List;

/** Serial registration while receiving goods, and serial lookup. */
public interface SerialScanService {

    List<SerialResponse> scan(Long receiptId, ScanSerialRequest request);

    void remove(Long receiptId, Long serialId);

    List<SerialResponse> getByReceiptDetail(Long detailId);

    SerialResponse lookup(String serialNumber);
}
