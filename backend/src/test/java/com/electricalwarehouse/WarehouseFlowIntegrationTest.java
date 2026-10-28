package com.electricalwarehouse;

import com.electricalwarehouse.dto.request.CreateStockInRequest;
import com.electricalwarehouse.dto.request.CreateStockOutRequest;
import com.electricalwarehouse.dto.request.PickGoodsRequest;
import com.electricalwarehouse.dto.request.ReportFilterRequest;
import com.electricalwarehouse.dto.request.ScanSerialRequest;
import com.electricalwarehouse.dto.response.StockInResponse;
import com.electricalwarehouse.dto.response.StockOutResponse;
import com.electricalwarehouse.dto.response.XNTReportResponse;
import com.electricalwarehouse.entity.*;
import com.electricalwarehouse.entity.enums.*;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.repository.*;
import com.electricalwarehouse.security.CustomUserDetailsService;
import com.electricalwarehouse.security.UserPrincipal;
import com.electricalwarehouse.service.InventoryService;
import com.electricalwarehouse.service.PickGoodsService;
import com.electricalwarehouse.service.ReportService;
import com.electricalwarehouse.service.SerialScanService;
import com.electricalwarehouse.service.StockInService;
import com.electricalwarehouse.service.StockOutService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** End-to-end business flow on H2: receive -> approve -> complete -> issue -> report. */
@SpringBootTest
@ActiveProfiles("test")
class WarehouseFlowIntegrationTest {

    @Autowired StockInService stockInService;
    @Autowired SerialScanService serialScanService;
    @Autowired StockOutService stockOutService;
    @Autowired PickGoodsService pickGoodsService;
    @Autowired InventoryService inventoryService;
    @Autowired ReportService reportService;
    @Autowired CustomUserDetailsService userDetailsService;
    @Autowired WarehouseRepository warehouseRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired ProductSerialRepository serialRepository;

    private Warehouse warehouse;
    private User staff;
    private User manager;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        warehouse = new Warehouse();
        warehouse.setCode("WH-" + suffix);
        warehouse.setName("Kho chính " + suffix);
        warehouseRepository.save(warehouse);
        staff = user("staff-" + suffix, RoleCode.NVK, RoleCode.NVTM, RoleCode.NVBH);
        manager = user("qlk-" + suffix, RoleCode.QLK);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void receiveThenIssueKeepsStockLedgerConsistent() {
        Product product = product(false);

        // Stock in 10 units.
        login(staff);
        StockInResponse in = stockInService.create(new CreateStockInRequest(warehouse.getId(), null, null, null, null,
                List.of(new CreateStockInRequest.Item(product.getId(), null, 10, new BigDecimal("100"), "LOT1", null))));
        stockInService.submit(in.id());
        // Separation of duties: the creator cannot approve.
        assertThatThrownBy(() -> stockInService.approve(in.id())).isInstanceOf(BusinessException.class);

        login(manager);
        stockInService.approve(in.id());
        assertThat(notificationRepository.countByRecipientIdAndReadFalse(staff.getId())).isPositive();

        login(staff);
        assertThat(stockInService.complete(in.id()).status()).isEqualTo(ReceiptStatus.COMPLETED);
        assertThat(inventoryService.getAvailableQuantity(warehouse.getId(), product.getId())).isEqualTo(10);

        // Stock out 4 units: approval reserves, completion deducts.
        StockOutResponse out = stockOutService.create(new CreateStockOutRequest(warehouse.getId(), null, OutType.SALE,
                null, null, List.of(new CreateStockOutRequest.Item(product.getId(), 4, null))));
        stockOutService.submit(out.id());
        login(manager);
        stockOutService.approve(out.id());
        assertThat(inventoryService.getAvailableQuantity(warehouse.getId(), product.getId())).isEqualTo(6);

        login(staff);
        Long detailId = out.details().get(0).id();
        pickGoodsService.pick(out.id(), new PickGoodsRequest(detailId, 4, null));
        assertThat(stockOutService.complete(out.id()).status()).isEqualTo(ReceiptStatus.COMPLETED);
        assertThat(inventoryService.getAvailableQuantity(warehouse.getId(), product.getId())).isEqualTo(6);

        // Over-issuing is refused.
        assertThatThrownBy(() -> stockOutService.create(new CreateStockOutRequest(warehouse.getId(), null,
                OutType.SALE, null, null, List.of(new CreateStockOutRequest.Item(product.getId(), 7, null)))))
                .isInstanceOf(BusinessException.class);

        login(manager);
        XNTReportResponse xnt = reportService.xntReport(new ReportFilterRequest(warehouse.getId(), null, null,
                LocalDate.now(), LocalDate.now()));
        XNTReportResponse.Row row = xnt.rows().stream().filter(r -> r.productId().equals(product.getId()))
                .findFirst().orElseThrow();
        assertThat(row.inQuantity()).isEqualTo(10);
        assertThat(row.outQuantity()).isEqualTo(4);
        assertThat(row.closingQuantity()).isEqualTo(6);
    }

    @Test
    void serialManagedGoodsRequireEverySerialBeforeCompletion() {
        Product product = product(true);

        login(staff);
        StockInResponse in = stockInService.create(new CreateStockInRequest(warehouse.getId(), null, null, null, null,
                List.of(new CreateStockInRequest.Item(product.getId(), null, 2, null, null, null))));
        stockInService.submit(in.id());
        login(manager);
        stockInService.approve(in.id());

        login(staff);
        Long detailId = in.details().get(0).id();
        String sn = "SN-" + UUID.randomUUID();
        serialScanService.scan(in.id(), new ScanSerialRequest(detailId, List.of(sn)));
        assertThatThrownBy(() -> stockInService.complete(in.id())).isInstanceOf(BusinessException.class);

        serialScanService.scan(in.id(), new ScanSerialRequest(detailId, List.of(sn + "-2")));
        stockInService.complete(in.id());
        assertThat(serialRepository.findBySerialNumber(sn).orElseThrow().getStatus()).isEqualTo(SerialStatus.IN_STOCK);
        assertThat(inventoryService.getAvailableQuantity(warehouse.getId(), product.getId())).isEqualTo(2);
    }

    private Product product(boolean serialManaged) {
        Product p = new Product();
        p.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));
        p.setName("Aptomat 1 pha");
        p.setSerialManaged(serialManaged);
        p.setCostPrice(new BigDecimal("100"));
        p.setSalePrice(new BigDecimal("150"));
        return productRepository.save(p);
    }

    private User user(String username, RoleCode... roles) {
        User u = new User();
        u.setUsername(username);
        u.setFullName(username);
        u.setPasswordHash("{noop}unused");
        userRepository.save(u);
        for (RoleCode role : roles) {
            userRoleRepository.save(new UserRole(u, roleRepository.findByCode(role).orElseThrow()));
        }
        return u;
    }

    private void login(User user) {
        UserPrincipal principal = userDetailsService.loadUserByUsername(user.getUsername());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
