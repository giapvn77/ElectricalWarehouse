package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.AuditLogResponse;
import com.electricalwarehouse.dto.response.DashboardPendingApprovalResponse;
import com.electricalwarehouse.dto.response.DashboardSummaryResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.service.approval.ApprovalService;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final ApprovalService approvalService;
    private final ReportService reportService;
    private final AuditService auditService;

    /** Phiếu nhập/xuất/chuyển kho/kiểm kê đang chờ duyệt, gộp chung một danh sách. */
    @GetMapping("/pending-approvals")
    public ApiResponse<DashboardPendingApprovalResponse> pendingApprovals() {
        return ApiResponse.ok(approvalService.getPendingApprovals());
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {
        return ApiResponse.ok(reportService.dashboardSummary());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PagedResult<AuditLogResponse>> auditLogs(@RequestParam(required = false) String entityType,
                                                                @RequestParam(required = false) Long entityId,
                                                                @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(auditService.search(entityType, entityId, pageable));
    }
}
