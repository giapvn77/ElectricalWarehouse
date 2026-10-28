package com.electricalwarehouse.job;

import com.electricalwarehouse.config.AppProperties;
import com.electricalwarehouse.entity.Inventory;
import com.electricalwarehouse.entity.InventoryThreshold;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.enums.NotificationType;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.event.StockChangedEvent;
import com.electricalwarehouse.repository.InventoryRepository;
import com.electricalwarehouse.repository.InventoryThresholdRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.List;

/**
 * Min-Max and expiry alerts for warehouse managers. Runs on a schedule and also right after
 * any stock change, so a low-stock alert does not wait for the next scan.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertJob {

    private final InventoryThresholdRepository thresholdRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AppProperties props;

    @Scheduled(cron = "${app.alert.cron}")
    @Transactional
    public void scan() {
        List<InventoryThreshold> thresholds = thresholdRepository.findAll();
        thresholds.forEach(this::check);

        LocalDate limit = LocalDate.now().plusDays(props.alert().expiryWarningDays());
        for (Inventory row : inventoryRepository.findExpiringBefore(limit)) {
            String title = "Sắp hết hạn: " + row.getProduct().getSku();
            String message = row.getQuantity() + " " + row.getProduct().getName() + " (lô " + row.getLotNumber()
                    + ") hết hạn ngày " + row.getExpiryDate() + " tại kho " + row.getWarehouse().getName();
            managers(row.getWarehouse().getId()).forEach(u ->
                    notificationService.notifyOnce(u, NotificationType.EXPIRY_WARNING, title, message, row.getId()));
        }
        log.debug("Inventory alert scan done: {} thresholds", thresholds.size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onStockChanged(StockChangedEvent e) {
        thresholdRepository.findByWarehouseIdAndProductId(e.warehouseId(), e.productId()).ifPresent(this::check);
    }

    private void check(InventoryThreshold t) {
        int qty = inventoryRepository.sumQuantity(t.getWarehouse().getId(), t.getProduct().getId());
        NotificationType type;
        String title;
        if (qty < t.getMinQuantity()) {
            type = NotificationType.LOW_STOCK;
            title = "Tồn thấp: " + t.getProduct().getSku();
        } else if (qty > t.getMaxQuantity()) {
            type = NotificationType.OVER_STOCK;
            title = "Tồn vượt mức: " + t.getProduct().getSku();
        } else {
            return;
        }
        String message = t.getProduct().getName() + " tại kho " + t.getWarehouse().getName() + " còn " + qty
                + " (min " + t.getMinQuantity() + ", max " + t.getMaxQuantity() + ")";
        managers(t.getWarehouse().getId()).forEach(u ->
                notificationService.notifyOnce(u, type, title, message, t.getId()));
    }

    private List<User> managers(Long warehouseId) {
        return userRepository.findActiveByRolesForWarehouse(List.of(RoleCode.QLK), warehouseId);
    }
}
