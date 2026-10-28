package com.electricalwarehouse.service.approval;

import com.electricalwarehouse.entity.ApprovableDocument;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Approval rules, including Separation of Duties: the person who creates a document
 * can never approve or reject it, whatever roles they hold.
 */
@Component
public class ApprovalPolicy {

    public void checkCanDecide(UserPrincipal approver, ApprovableDocument doc, Long warehouseId) {
        if (doc.getStatus() != ReceiptStatus.PENDING) {
            throw new BusinessException("Chỉ duyệt được phiếu đang chờ duyệt (hiện tại: " + doc.getStatus() + ")");
        }
        boolean isAdmin = approver.hasRole(RoleCode.ADMIN);
        if (!isAdmin && !approver.hasRole(RoleCode.QLK)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Chỉ Quản lý kho được duyệt phiếu");
        }
        if (doc.getCreatedBy().getId().equals(approver.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Người lập phiếu không được tự duyệt phiếu của mình (phân tách nhiệm vụ)");
        }
        if (!isAdmin && approver.warehouseId() != null && !approver.warehouseId().equals(warehouseId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Bạn chỉ được duyệt phiếu của kho mình quản lý");
        }
    }

    /** Draft documents may be edited/submitted by their creator (or an admin). */
    public void checkCanEdit(UserPrincipal user, ApprovableDocument doc) {
        if (doc.getStatus() != ReceiptStatus.DRAFT) {
            throw new BusinessException("Chỉ sửa được phiếu ở trạng thái nháp");
        }
        checkOwnerOrAdmin(user, doc);
    }

    public void checkCanCancel(UserPrincipal user, ApprovableDocument doc) {
        if (doc.getStatus() != ReceiptStatus.DRAFT && doc.getStatus() != ReceiptStatus.PENDING) {
            throw new BusinessException("Chỉ hủy được phiếu nháp hoặc đang chờ duyệt");
        }
        checkOwnerOrAdmin(user, doc);
    }

    /** Users bound to a warehouse may only operate on that warehouse. */
    public void checkWarehouseAccess(UserPrincipal user, Long warehouseId) {
        if (!user.hasRole(RoleCode.ADMIN) && user.warehouseId() != null && !user.warehouseId().equals(warehouseId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Bạn không thuộc kho này");
        }
    }

    private void checkOwnerOrAdmin(UserPrincipal user, ApprovableDocument doc) {
        if (!user.hasRole(RoleCode.ADMIN) && !doc.getCreatedBy().getId().equals(user.id())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Chỉ người lập phiếu được thao tác");
        }
    }
}
