package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.StorageLocationRequest;
import com.electricalwarehouse.dto.request.WarehouseRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.StorageLocationResponse;
import com.electricalwarehouse.dto.response.WarehouseResponse;
import com.electricalwarehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private static final String MANAGE = "hasAnyRole('ADMIN', 'QLK')";

    private final WarehouseService warehouseService;

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> list(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(warehouseService.getAll(activeOnly));
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(warehouseService.get(id));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    public ApiResponse<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        return ApiResponse.ok(warehouseService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE)
    public ApiResponse<WarehouseResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        return ApiResponse.ok(warehouseService.save(id, request));
    }

    @GetMapping("/{id}/locations")
    public ApiResponse<List<StorageLocationResponse>> locations(@PathVariable Long id) {
        return ApiResponse.ok(warehouseService.getLocations(id));
    }

    @PostMapping("/{id}/locations")
    @PreAuthorize(MANAGE)
    public ApiResponse<StorageLocationResponse> createLocation(@PathVariable Long id,
                                                               @Valid @RequestBody StorageLocationRequest request) {
        return ApiResponse.ok(warehouseService.saveLocation(id, null, request));
    }

    @PutMapping("/{id}/locations/{locationId}")
    @PreAuthorize(MANAGE)
    public ApiResponse<StorageLocationResponse> updateLocation(@PathVariable Long id, @PathVariable Long locationId,
                                                               @Valid @RequestBody StorageLocationRequest request) {
        return ApiResponse.ok(warehouseService.saveLocation(id, locationId, request));
    }
}
