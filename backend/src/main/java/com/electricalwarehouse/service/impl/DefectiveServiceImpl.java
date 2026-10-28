package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.DefectRegistrationRequest;
import com.electricalwarehouse.dto.request.DefectResolutionRequest;
import com.electricalwarehouse.dto.response.DefectiveGoodResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.*;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.DefectiveGoodRepository;
import com.electricalwarehouse.repository.ProductSerialRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.WarehouseRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.DefectiveService;
import com.electricalwarehouse.service.InventoryService;
import com.electricalwarehouse.service.Lookup;
import com.electricalwarehouse.service.StockMovement;
import com.electricalwarehouse.service.approval.ApprovalPolicy;
import com.electricalwarehouse.util.CodeGenerator;
import com.electricalwarehouse.util.FifoFefoCalculator.Pool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DefectiveServiceImpl implements DefectiveService {

    /** Goods coming back from customers were already issued, so nothing leaves the source warehouse. */
    private static final Set<DefectSource> FROM_CUSTOMER = Set.of(DefectSource.CUSTOMER_RETURN, DefectSource.WARRANTY);

    private final DefectiveGoodRepository defectRepository;
    private final ProductSerialRepository serialRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ApprovalPolicy approvalPolicy;
    private final AuditService auditService;
    private final Lookup lookup;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<DefectiveGoodResponse> search(DefectStatus status, Long warehouseId, Pageable pageable) {
        return PagedResult.of(defectRepository.search(status, warehouseId, pageable), mapper::toDefectiveGoodResponse);
    }

    @Override
    @Transactional
    public DefectiveGoodResponse register(DefectRegistrationRequest req) {
        approvalPolicy.checkWarehouseAccess(CurrentUser.get(), req.warehouseId());
        Warehouse source = lookup.warehouse(req.warehouseId());
        Product product = lookup.product(req.productId());
        Warehouse defectiveWh = warehouseRepository.findFirstByTypeAndActiveTrue(WarehouseType.DEFECTIVE)
                .orElseThrow(() -> new BusinessException("Chưa khai báo kho hàng lỗi (loại DEFECTIVE)"));

        DefectiveGood defect = new DefectiveGood();
        defect.setCode(CodeGenerator.next(CodeGenerator.DEFECT));
        defect.setProduct(product);
        defect.setSourceWarehouse(source);
        defect.setDefectiveWarehouse(defectiveWh);
        defect.setCustomer(lookup.customer(req.customerId()));
        defect.setSupplier(lookup.supplier(req.supplierId()));
        defect.setSource(req.source());
        defect.setDescription(req.description());
        defect.setReportedBy(userRepository.getReferenceById(CurrentUser.id()));
        defect.setQuantity(req.quantity());
        defectRepository.save(defect);

        StockMovement out = StockMovement.of(source, product, req.quantity(), TransactionType.DEFECT,
                ReferenceType.DEFECT, defect.getId(), defect.getCode());
        ProductSerial serial = null;
        if (product.isSerialManaged()) {
            if (req.serialNumber() == null || req.quantity() != 1) {
                throw new BusinessException("Hàng quản lý serial: khai báo từng serial, số lượng = 1");
            }
            serial = serialRepository.findBySerialNumber(req.serialNumber().trim())
                    .orElseThrow(() -> new NotFoundException("Serial", req.serialNumber()));
            if (!serial.getProduct().getId().equals(product.getId())) {
                throw new BusinessException("Serial không thuộc sản phẩm đã chọn");
            }
            defect.setSerial(serial);
        }

        if (!FROM_CUSTOMER.contains(req.source())) {
            if (serial != null) {
                if (serial.getStatus() != SerialStatus.IN_STOCK || serial.getWarehouse() == null
                        || !serial.getWarehouse().getId().equals(source.getId())) {
                    throw new BusinessException("Serial không có sẵn trong kho đã chọn");
                }
                inventoryService.decreaseAt(out.at(serial.getLocation(), serial.getLotNumber(), null));
            } else {
                inventoryService.decrease(out, Pool.AVAILABLE);
            }
        }
        inventoryService.increase(StockMovement.of(defectiveWh, product, req.quantity(), TransactionType.DEFECT,
                ReferenceType.DEFECT, defect.getId(), defect.getCode()));
        if (serial != null) {
            serial.setWarehouse(defectiveWh);
            serial.setLocation(null);
            serial.setStatus(SerialStatus.DEFECTIVE);
        }
        defect.setStatus(DefectStatus.IN_DEFECTIVE_WAREHOUSE);
        auditService.log("REGISTER", "DEFECT", defect.getId(), req.source() + ": " + req.description());
        return mapper.toDefectiveGoodResponse(defect);
    }

    @Override
    @Transactional
    public DefectiveGoodResponse resolve(Long id, DefectResolutionRequest req) {
        DefectiveGood defect = defectRepository.findById(id).orElseThrow(() -> new NotFoundException("Hàng lỗi", id));
        if (defect.getStatus() != DefectStatus.IN_DEFECTIVE_WAREHOUSE) {
            throw new BusinessException("Hàng lỗi đã được xử lý (" + defect.getStatus() + ")");
        }
        DefectStatus target = req.status();
        if (target == DefectStatus.REPORTED || target == DefectStatus.IN_DEFECTIVE_WAREHOUSE) {
            throw new BusinessException("Trạng thái xử lý không hợp lệ");
        }

        Product product = defect.getProduct();
        Warehouse defectiveWh = defect.getDefectiveWarehouse();
        StockMovement out = StockMovement.of(defectiveWh, product, defect.getQuantity(), TransactionType.DEFECT,
                ReferenceType.DEFECT, defect.getId(), defect.getCode());
        inventoryService.decrease(out, Pool.AVAILABLE);

        ProductSerial serial = defect.getSerial();
        if (target == DefectStatus.REPAIRED) {
            // Repaired goods go back to the warehouse they came from.
            inventoryService.increase(StockMovement.of(defect.getSourceWarehouse(), product, defect.getQuantity(),
                    TransactionType.DEFECT, ReferenceType.DEFECT, defect.getId(), defect.getCode()));
            if (serial != null) {
                serial.setWarehouse(defect.getSourceWarehouse());
                serial.setStatus(SerialStatus.IN_STOCK);
            }
        } else if (serial != null) {
            serial.setWarehouse(null);
            serial.setStatus(target == DefectStatus.RETURNED_TO_SUPPLIER ? SerialStatus.RETURNED : SerialStatus.LOST);
        }

        defect.setStatus(target);
        defect.setResolutionNote(req.note());
        defect.setResolvedAt(LocalDateTime.now());
        auditService.log("RESOLVE", "DEFECT", defect.getId(), target + (req.note() != null ? ": " + req.note() : ""));
        return mapper.toDefectiveGoodResponse(defect);
    }
}
