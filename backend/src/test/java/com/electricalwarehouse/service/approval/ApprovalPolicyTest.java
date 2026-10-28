package com.electricalwarehouse.service.approval;

import com.electricalwarehouse.entity.StockInReceipt;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalPolicyTest {

    private final ApprovalPolicy policy = new ApprovalPolicy();

    private static UserPrincipal principal(long id, Long warehouseId, RoleCode... roles) {
        return new UserPrincipal(id, "u" + id, "x", true, warehouseId, Set.of(roles), Set.of());
    }

    private static StockInReceipt pendingReceiptCreatedBy(long userId) {
        User creator = new User();
        creator.setId(userId);
        StockInReceipt receipt = new StockInReceipt();
        receipt.setCreatedBy(creator);
        receipt.setStatus(ReceiptStatus.PENDING);
        return receipt;
    }

    @Test
    void creatorCannotApproveOwnDocumentEvenAsAdmin() {
        StockInReceipt receipt = pendingReceiptCreatedBy(1);
        assertThatThrownBy(() -> policy.checkCanDecide(principal(1, null, RoleCode.ADMIN, RoleCode.QLK), receipt, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phân tách nhiệm vụ");
    }

    @Test
    void managerOfAnotherWarehouseCannotApprove() {
        StockInReceipt receipt = pendingReceiptCreatedBy(1);
        assertThatThrownBy(() -> policy.checkCanDecide(principal(2, 99L, RoleCode.QLK), receipt, 10L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void staffWithoutManagerRoleCannotApprove() {
        StockInReceipt receipt = pendingReceiptCreatedBy(1);
        assertThatThrownBy(() -> policy.checkCanDecide(principal(2, 10L, RoleCode.NVK), receipt, 10L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void managerOfSameWarehouseCanApprove() {
        StockInReceipt receipt = pendingReceiptCreatedBy(1);
        assertThatCode(() -> policy.checkCanDecide(principal(2, 10L, RoleCode.QLK), receipt, 10L))
                .doesNotThrowAnyException();
    }

    @Test
    void onlyPendingDocumentsCanBeDecided() {
        StockInReceipt receipt = pendingReceiptCreatedBy(1);
        receipt.setStatus(ReceiptStatus.DRAFT);
        assertThatThrownBy(() -> policy.checkCanDecide(principal(2, null, RoleCode.QLK), receipt, 10L))
                .isInstanceOf(BusinessException.class);
    }
}
