package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.ScanSerialRequest;
import com.electricalwarehouse.dto.response.SerialResponse;
import com.electricalwarehouse.entity.Product;
import com.electricalwarehouse.entity.ProductSerial;
import com.electricalwarehouse.entity.StockInReceipt;
import com.electricalwarehouse.entity.StockInReceiptDetail;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.SerialStatus;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.ProductSerialRepository;
import com.electricalwarehouse.repository.StockInReceiptRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.SerialScanService;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SerialScanServiceImpl implements SerialScanService {

    private final StockInReceiptRepository receiptRepository;
    private final ProductSerialRepository serialRepository;
    private final ApprovalPolicy approvalPolicy;
    private final AuditService auditService;
    private final DTOMapper mapper;

    /**
     * Registers serials against an approved receipt line. Until the receipt is completed the serials
     * stay RESERVED (scanned, not yet in stock).
     */
    @Override
    @Transactional
    public List<SerialResponse> scan(Long receiptId, ScanSerialRequest req) {
        StockInReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new NotFoundException("Phiếu nhập", receiptId));
        if (receipt.getStatus() != ReceiptStatus.APPROVED) {
            throw new BusinessException("Chỉ quét serial cho phiếu nhập đã duyệt");
        }
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), receipt.getWarehouse().getId());
        StockInReceiptDetail detail = receipt.getDetails().stream()
                .filter(d -> d.getId().equals(req.detailId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Dòng phiếu nhập", req.detailId()));
        Product product = detail.getProduct();
        if (!product.isSerialManaged()) {
            throw new BusinessException("Sản phẩm " + product.getSku() + " không quản lý theo serial");
        }

        Set<String> seen = new HashSet<>();
        List<ProductSerial> created = new ArrayList<>();
        for (String raw : req.serialNumbers()) {
            String serial = raw.trim();
            if (!seen.add(serial)) {
                throw new BusinessException("Serial bị lặp trong lần quét: " + serial);
            }
            if (serialRepository.existsBySerialNumber(serial)) {
                throw new BusinessException("Serial đã tồn tại trong hệ thống: " + serial);
            }
            if (detail.getScannedQuantity() + created.size() >= detail.getQuantity()) {
                throw new BusinessException("Đã quét đủ " + detail.getQuantity() + " serial cho " + product.getSku());
            }
            ProductSerial s = new ProductSerial();
            s.setProduct(product);
            s.setSerialNumber(serial);
            s.setWarehouse(receipt.getWarehouse());
            s.setLocation(detail.getLocation());
            s.setStockInDetail(detail);
            s.setStatus(SerialStatus.RESERVED);
            s.setLotNumber(detail.getLotNumber());
            s.setExpiryDate(detail.getExpiryDate());
            if (product.getWarrantyMonths() != null) {
                s.setWarrantyUntil(receipt.getReceiptDate().plusMonths(product.getWarrantyMonths()));
            }
            created.add(s);
        }
        serialRepository.saveAll(created);
        detail.setScannedQuantity(detail.getScannedQuantity() + created.size());
        auditService.log("SCAN_SERIAL", "STOCK_IN", receipt.getId(), created.size() + " serial cho " + product.getSku());
        return created.stream().map(mapper::toSerialResponse).toList();
    }

    @Override
    @Transactional
    public void remove(Long receiptId, Long serialId) {
        ProductSerial serial = serialRepository.findById(serialId)
                .orElseThrow(() -> new NotFoundException("Serial", serialId));
        StockInReceiptDetail detail = serial.getStockInDetail();
        if (detail == null || !detail.getReceipt().getId().equals(receiptId)
                || detail.getReceipt().getStatus() != ReceiptStatus.APPROVED) {
            throw new BusinessException("Chỉ xóa được serial vừa quét của phiếu nhập chưa hoàn tất");
        }
        detail.setScannedQuantity(detail.getScannedQuantity() - 1);
        serialRepository.delete(serial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerialResponse> getByReceiptDetail(Long detailId) {
        return serialRepository.findByStockInDetailId(detailId).stream().map(mapper::toSerialResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SerialResponse lookup(String serialNumber) {
        return serialRepository.findBySerialNumber(serialNumber.trim()).map(mapper::toSerialResponse)
                .orElseThrow(() -> new NotFoundException("Serial", serialNumber));
    }
}
