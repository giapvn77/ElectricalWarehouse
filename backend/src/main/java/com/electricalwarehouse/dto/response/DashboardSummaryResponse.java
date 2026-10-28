package com.electricalwarehouse.dto.response;

public record DashboardSummaryResponse(
        long productCount,
        long warehouseCount,
        long totalStockQuantity,
        long lowStockCount,
        long openDefectCount,
        long unreadNotifications
) {}
