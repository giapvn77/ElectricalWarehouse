package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.ReportFilterRequest;
import com.electricalwarehouse.dto.response.DashboardSummaryResponse;
import com.electricalwarehouse.dto.response.InventoryReportResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.StockTransactionResponse;
import com.electricalwarehouse.dto.response.XNTReportResponse;
import com.electricalwarehouse.integration.ExportFormat;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    InventoryReportResponse inventoryReport(Long warehouseId);

    PagedResult<StockTransactionResponse> movementReport(ReportFilterRequest filter, Pageable pageable);

    /** Nhập - Xuất - Tồn for the period. */
    XNTReportResponse xntReport(ReportFilterRequest filter);

    byte[] exportInventory(Long warehouseId, ExportFormat format);

    byte[] exportMovement(ReportFilterRequest filter, ExportFormat format);

    byte[] exportXnt(ReportFilterRequest filter, ExportFormat format);

    DashboardSummaryResponse dashboardSummary();
}
