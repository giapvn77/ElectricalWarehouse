package com.electricalwarehouse.service;

import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.Warehouse;
import com.electricalwarehouse.entity.enums.NotificationType;
import com.electricalwarehouse.entity.enums.ReceiptStatus;
import com.electricalwarehouse.entity.enums.ReferenceType;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.event.ReceiptStatusChangedEvent;
import com.electricalwarehouse.integration.EmailService;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/** Turns approval workflow events into in-app notifications and e-mails, after the change is committed. */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStatusChanged(ReceiptStatusChangedEvent e) {
        String label = label(e.referenceType()) + " " + e.documentCode();
        if (e.newStatus() == ReceiptStatus.PENDING) {
            List<User> managers = userRepository.findActiveByRolesForWarehouse(List.of(RoleCode.QLK), e.warehouseId());
            managers.stream().filter(u -> !u.getId().equals(e.creatorId())).forEach(u ->
                    notificationService.notify(u, NotificationType.APPROVAL_REQUEST, label + " chờ duyệt",
                            "Có phiếu mới cần bạn duyệt.", e.referenceType(), e.documentId()));
            warehouseRepository.findById(e.warehouseId()).map(Warehouse::getManagerEmail).ifPresent(email ->
                    emailService.send(email, "[Kho] " + label + " chờ duyệt",
                            label + " vừa được gửi duyệt. Vui lòng đăng nhập hệ thống để xử lý."));
        } else if (e.newStatus() == ReceiptStatus.APPROVED || e.newStatus() == ReceiptStatus.REJECTED) {
            boolean approved = e.newStatus() == ReceiptStatus.APPROVED;
            userRepository.findById(e.creatorId()).ifPresent(creator -> {
                String title = label + (approved ? " đã được duyệt" : " bị từ chối");
                String message = approved ? "Phiếu của bạn đã được duyệt." : "Lý do: " + e.reason();
                notificationService.notify(creator, approved ? NotificationType.APPROVED : NotificationType.REJECTED,
                        title, message, e.referenceType(), e.documentId());
                emailService.send(creator.getEmail(), "[Kho] " + title, message);
            });
        }
    }

    private static String label(ReferenceType type) {
        return switch (type) {
            case STOCK_IN -> "Phiếu nhập";
            case STOCK_OUT -> "Phiếu xuất";
            case TRANSFER -> "Phiếu chuyển kho";
            case ADJUSTMENT -> "Phiếu kiểm kê";
            case DEFECT -> "Hàng lỗi";
        };
    }
}
