package com.electricalwarehouse.service.approval;

import com.electricalwarehouse.dto.response.DashboardPendingApprovalResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.event.ReceiptStatusChangedEvent;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.repository.*;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.security.UserPrincipal;
import com.electricalwarehouse.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Status transitions shared by every approval document. Domain services call these and then
 * apply their own stock effects inside the same transaction.
 */
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private static final Set<ReceiptStatus> PENDING = Set.of(ReceiptStatus.PENDING);

    private final ApprovalPolicy policy;
    private final AuditService auditService;
    private final ApplicationEventPublisher events;
    private final UserRepository userRepository;
    private final StockInReceiptRepository stockInRepository;
    private final StockOutReceiptRepository stockOutRepository;
    private final StockTransferRepository transferRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;

    @Transactional
    public void submit(ApprovableDocument doc, ReferenceType type, Long warehouseId) {
        policy.checkCanEdit(CurrentUser.get(), doc);
        changeStatus(doc, type, warehouseId, ReceiptStatus.PENDING, null);
    }

    @Transactional
    public void approve(ApprovableDocument doc, ReferenceType type, Long warehouseId) {
        UserPrincipal approver = CurrentUser.get();
        policy.checkCanDecide(approver, doc, warehouseId);
        doc.setApprovedBy(userRepository.getReferenceById(approver.id()));
        doc.setApprovedAt(LocalDateTime.now());
        changeStatus(doc, type, warehouseId, ReceiptStatus.APPROVED, null);
    }

    @Transactional
    public void reject(ApprovableDocument doc, ReferenceType type, Long warehouseId, String reason) {
        UserPrincipal approver = CurrentUser.get();
        policy.checkCanDecide(approver, doc, warehouseId);
        doc.setApprovedBy(userRepository.getReferenceById(approver.id()));
        doc.setApprovedAt(LocalDateTime.now());
        doc.setRejectReason(reason);
        changeStatus(doc, type, warehouseId, ReceiptStatus.REJECTED, reason);
    }

    @Transactional
    public void cancel(ApprovableDocument doc, ReferenceType type, Long warehouseId) {
        policy.checkCanCancel(CurrentUser.get(), doc);
        changeStatus(doc, type, warehouseId, ReceiptStatus.CANCELLED, null);
    }

    /** Moves a document to any status (used for PICKING/COMPLETED steps) with audit + event. */
    @Transactional
    public void changeStatus(ApprovableDocument doc, ReferenceType type, Long warehouseId,
                             ReceiptStatus newStatus, String reason) {
        ReceiptStatus old = doc.getStatus();
        if (old == newStatus) {
            return;
        }
        if (old == ReceiptStatus.COMPLETED || old == ReceiptStatus.CANCELLED || old == ReceiptStatus.REJECTED) {
            throw new BusinessException("Phiếu đã kết thúc (" + old + "), không thể chuyển trạng thái");
        }
        doc.setStatus(newStatus);
        UserPrincipal actor = CurrentUser.orNull();
        auditService.log("STATUS_" + newStatus, type.name(), doc.getId(), old + " -> " + newStatus
                + (reason != null ? " | " + reason : ""));
        events.publishEvent(new ReceiptStatusChangedEvent(type, doc.getId(), doc.getCode(), warehouseId, old,
                newStatus, actor != null ? actor.id() : null, doc.getCreatedBy().getId(), reason));
    }

    /** All documents waiting for approval, oldest first, for the dashboard. */
    @Transactional(readOnly = true)
    public DashboardPendingApprovalResponse getPendingApprovals() {
        List<DashboardPendingApprovalResponse.Item> items = new ArrayList<>();
        stockInRepository.findTop20ByStatusInOrderByCreatedAtAsc(PENDING)
                .forEach(r -> items.add(item(ReferenceType.STOCK_IN, r, r.getWarehouse())));
        stockOutRepository.findTop20ByStatusInOrderByCreatedAtAsc(PENDING)
                .forEach(r -> items.add(item(ReferenceType.STOCK_OUT, r, r.getWarehouse())));
        transferRepository.findTop20ByStatusInOrderByCreatedAtAsc(PENDING)
                .forEach(r -> items.add(item(ReferenceType.TRANSFER, r, r.getFromWarehouse())));
        adjustmentRepository.findTop20ByStatusInOrderByCreatedAtAsc(PENDING)
                .forEach(r -> items.add(item(ReferenceType.ADJUSTMENT, r, r.getWarehouse())));
        items.sort(Comparator.comparing(DashboardPendingApprovalResponse.Item::createdAt));

        long in = stockInRepository.countByStatus(ReceiptStatus.PENDING);
        long out = stockOutRepository.countByStatus(ReceiptStatus.PENDING);
        long transfer = transferRepository.countByStatus(ReceiptStatus.PENDING);
        long adjustment = adjustmentRepository.countByStatus(ReceiptStatus.PENDING);
        return new DashboardPendingApprovalResponse(in, out, transfer, adjustment, in + out + transfer + adjustment,
                items);
    }

    private static DashboardPendingApprovalResponse.Item item(ReferenceType type, ApprovableDocument doc, Warehouse wh) {
        return new DashboardPendingApprovalResponse.Item(type, doc.getId(), doc.getCode(), wh.getName(),
                doc.getCreatedBy().getFullName(), doc.getStatus(), doc.getCreatedAt());
    }
}
