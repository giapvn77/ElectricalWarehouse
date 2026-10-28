package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.ReportFilterRequest;
import com.electricalwarehouse.dto.response.*;
import com.electricalwarehouse.entity.InventoryThreshold;
import com.electricalwarehouse.entity.Product;
import com.electricalwarehouse.entity.Warehouse;
import com.electricalwarehouse.entity.enums.DefectStatus;
import com.electricalwarehouse.integration.ExcelExportService;
import com.electricalwarehouse.integration.ExportFormat;
import com.electricalwarehouse.integration.PdfExportService;
import com.electricalwarehouse.integration.TableData;
import com.electricalwarehouse.repository.*;
import com.electricalwarehouse.repository.InventoryRepository.StockSummaryView;
import com.electricalwarehouse.repository.StockTransactionRepository.XntView;
import com.electricalwarehouse.service.NotificationService;
import com.electricalwarehouse.service.ReportService;
import com.electricalwarehouse.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    /** Upper bound for exporting the movement ledger in one file. */
    private static final int MAX_EXPORT_ROWS = 20_000;

    private final InventoryRepository inventoryRepository;
    private final StockTransactionRepository transactionRepository;
    private final InventoryThresholdRepository thresholdRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final DefectiveGoodRepository defectRepository;
    private final StockTransactionService transactionService;
    private final NotificationService notificationService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    @Override
    @Transactional(readOnly = true)
    public InventoryReportResponse inventoryReport(Long warehouseId) {
        List<StockSummaryView> summary = inventoryRepository.summarize(warehouseId);
        Map<Long, Product> products = productsById(summary.stream().map(StockSummaryView::getProductId).toList());
        Map<Long, Warehouse> warehouses = warehouseRepository.findAll().stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
        Map<String, InventoryThreshold> thresholds = thresholdRepository.findAll().stream()
                .collect(Collectors.toMap(t -> t.getWarehouse().getId() + ":" + t.getProduct().getId(),
                        Function.identity()));

        List<InventoryReportResponse.Row> rows = new ArrayList<>();
        long totalQty = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        for (StockSummaryView s : summary) {
            Product p = products.get(s.getProductId());
            long qty = s.getQuantity();
            BigDecimal cost = Objects.requireNonNullElse(p.getCostPrice(), BigDecimal.ZERO);
            BigDecimal value = cost.multiply(BigDecimal.valueOf(qty));
            InventoryThreshold t = thresholds.get(s.getWarehouseId() + ":" + s.getProductId());
            rows.add(new InventoryReportResponse.Row(warehouses.get(s.getWarehouseId()).getName(), p.getSku(),
                    p.getName(), p.getUnit() != null ? p.getUnit().getName() : null, qty,
                    t != null ? t.getMinQuantity() : null, t != null ? t.getMaxQuantity() : null, cost, value));
            totalQty += qty;
            totalValue = totalValue.add(value);
        }
        rows.sort(Comparator.comparing(InventoryReportResponse.Row::warehouseName)
                .thenComparing(InventoryReportResponse.Row::sku));
        String whName = warehouseId != null ? warehouses.get(warehouseId).getName() : null;
        return new InventoryReportResponse(warehouseId, whName, rows, totalQty, totalValue);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<StockTransactionResponse> movementReport(ReportFilterRequest filter, Pageable pageable) {
        return transactionService.search(filter, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public XNTReportResponse xntReport(ReportFilterRequest f) {
        List<XntView> data = transactionRepository.aggregateXnt(f.warehouseId(), f.fromOrDefault().atStartOfDay(),
                f.toOrDefault().plusDays(1).atStartOfDay());
        Map<Long, Product> products = productsById(data.stream().map(XntView::getProductId).toList());
        List<XNTReportResponse.Row> rows = data.stream()
                .map(v -> {
                    Product p = products.get(v.getProductId());
                    long opening = nz(v.getOpeningQty());
                    long in = nz(v.getInQty());
                    long out = nz(v.getOutQty());
                    return new XNTReportResponse.Row(p.getId(), p.getSku(), p.getName(),
                            p.getUnit() != null ? p.getUnit().getName() : null, opening, in, out, opening + in - out);
                })
                .filter(r -> r.openingQuantity() != 0 || r.inQuantity() != 0 || r.outQuantity() != 0)
                .sorted(Comparator.comparing(XNTReportResponse.Row::sku))
                .toList();
        String whName = f.warehouseId() != null
                ? warehouseRepository.findById(f.warehouseId()).map(Warehouse::getName).orElse(null) : null;
        return new XNTReportResponse(f.fromOrDefault(), f.toOrDefault(), f.warehouseId(), whName, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportInventory(Long warehouseId, ExportFormat format) {
        InventoryReportResponse r = inventoryReport(warehouseId);
        List<List<Object>> rows = r.rows().stream().map(x -> List.<Object>of(x.warehouseName(), x.sku(),
                x.productName(), nvl(x.unitName()), x.quantity(), x.costPrice(), x.value())).toList();
        return export(new TableData("BÁO CÁO TỒN KHO",
                List.of("Kho: " + (r.warehouseName() != null ? r.warehouseName() : "Tất cả"),
                        "Tổng số lượng: " + r.totalQuantity() + " - Tổng giá trị: " + r.totalValue()),
                List.of("Kho", "Mã hàng", "Tên hàng", "ĐVT", "Số lượng", "Giá vốn", "Giá trị"), rows), format);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportMovement(ReportFilterRequest f, ExportFormat format) {
        PagedResult<StockTransactionResponse> page = transactionService.search(f,
                PageRequest.of(0, MAX_EXPORT_ROWS, Sort.by("transactionDate")));
        List<List<Object>> rows = page.content().stream().map(t -> List.<Object>of(t.transactionDate(),
                t.type().name(), t.warehouseName(), t.sku(), t.productName(), nvl(t.lotNumber()), t.quantity(),
                t.balanceAfter(), nvl(t.referenceCode()), nvl(t.performedByName()))).toList();
        return export(new TableData("BÁO CÁO BIẾN ĐỘNG KHO",
                List.of("Từ " + f.fromOrDefault() + " đến " + f.toOrDefault()),
                List.of("Thời gian", "Loại", "Kho", "Mã hàng", "Tên hàng", "Lô", "SL", "Tồn sau", "Chứng từ",
                        "Người thực hiện"), rows), format);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportXnt(ReportFilterRequest f, ExportFormat format) {
        XNTReportResponse r = xntReport(f);
        List<List<Object>> rows = r.rows().stream().map(x -> List.<Object>of(x.sku(), x.productName(),
                nvl(x.unitName()), x.openingQuantity(), x.inQuantity(), x.outQuantity(), x.closingQuantity())).toList();
        return export(new TableData("BÁO CÁO NHẬP - XUẤT - TỒN",
                List.of("Kho: " + (r.warehouseName() != null ? r.warehouseName() : "Tất cả"),
                        "Từ " + r.fromDate() + " đến " + r.toDate()),
                List.of("Mã hàng", "Tên hàng", "ĐVT", "Tồn đầu", "Nhập", "Xuất", "Tồn cuối"), rows), format);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse dashboardSummary() {
        List<StockSummaryView> summary = inventoryRepository.summarize(null);
        long totalQty = summary.stream().mapToLong(StockSummaryView::getQuantity).sum();
        Map<String, Long> qtyByKey = summary.stream().collect(Collectors.toMap(
                s -> s.getWarehouseId() + ":" + s.getProductId(), StockSummaryView::getQuantity));
        long lowStock = thresholdRepository.findAll().stream()
                .filter(t -> qtyByKey.getOrDefault(t.getWarehouse().getId() + ":" + t.getProduct().getId(), 0L)
                        < t.getMinQuantity())
                .count();
        long openDefects = defectRepository.search(DefectStatus.IN_DEFECTIVE_WAREHOUSE, null, Pageable.unpaged())
                .getTotalElements();
        return new DashboardSummaryResponse(productRepository.count(), warehouseRepository.count(), totalQty, lowStock,
                openDefects, notificationService.countUnread());
    }

    private byte[] export(TableData table, ExportFormat format) {
        return format == ExportFormat.PDF ? pdfExportService.export(table) : excelExportService.export(table);
    }

    private Map<Long, Product> productsById(Collection<Long> ids) {
        return productRepository.findAllById(new HashSet<>(ids)).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private static long nz(Long v) {
        return v == null ? 0 : v;
    }

    private static Object nvl(Object v) {
        return v == null ? "" : v;
    }
}
