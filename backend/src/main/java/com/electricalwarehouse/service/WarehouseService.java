package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.StorageLocationRequest;
import com.electricalwarehouse.dto.request.WarehouseRequest;
import com.electricalwarehouse.dto.response.StorageLocationResponse;
import com.electricalwarehouse.dto.response.WarehouseResponse;

import java.util.List;

/** Warehouses and their storage locations (khu - kệ - tầng). */
public interface WarehouseService {

    List<WarehouseResponse> getAll(boolean activeOnly);

    WarehouseResponse get(Long id);

    WarehouseResponse save(Long id, WarehouseRequest request);

    List<StorageLocationResponse> getLocations(Long warehouseId);

    StorageLocationResponse saveLocation(Long warehouseId, Long locationId, StorageLocationRequest request);
}
