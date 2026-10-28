package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.StorageLocationRequest;
import com.electricalwarehouse.dto.request.WarehouseRequest;
import com.electricalwarehouse.dto.response.StorageLocationResponse;
import com.electricalwarehouse.dto.response.WarehouseResponse;
import com.electricalwarehouse.entity.StorageLocation;
import com.electricalwarehouse.entity.Warehouse;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.StorageLocationRepository;
import com.electricalwarehouse.repository.WarehouseRepository;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StorageLocationRepository locationRepository;
    private final AuditService auditService;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll(boolean activeOnly) {
        List<Warehouse> list = activeOnly ? warehouseRepository.findByActiveTrue()
                : warehouseRepository.findAll(Sort.by("code"));
        return list.stream().map(mapper::toWarehouseResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse get(Long id) {
        return mapper.toWarehouseResponse(find(id));
    }

    @Override
    @Transactional
    public WarehouseResponse save(Long id, WarehouseRequest req) {
        Warehouse wh = id == null ? new Warehouse() : find(id);
        boolean codeChanged = id == null || !wh.getCode().equals(req.code());
        if (codeChanged && warehouseRepository.existsByCode(req.code())) {
            throw new BusinessException("Mã kho đã tồn tại");
        }
        mapper.updateWarehouse(req, wh);
        warehouseRepository.save(wh);
        auditService.log(id == null ? "CREATE" : "UPDATE", "WAREHOUSE", wh.getId(), wh.getCode());
        return mapper.toWarehouseResponse(wh);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageLocationResponse> getLocations(Long warehouseId) {
        return locationRepository.findByWarehouseIdOrderByCode(warehouseId).stream()
                .map(mapper::toLocationResponse).toList();
    }

    @Override
    @Transactional
    public StorageLocationResponse saveLocation(Long warehouseId, Long locationId, StorageLocationRequest req) {
        Warehouse wh = find(warehouseId);
        StorageLocation loc = locationId == null ? new StorageLocation()
                : locationRepository.findById(locationId).orElseThrow(() -> new NotFoundException("Vị trí", locationId));
        if (locationId != null && !loc.getWarehouse().getId().equals(warehouseId)) {
            throw new BusinessException("Vị trí không thuộc kho này");
        }
        boolean codeChanged = locationId == null || !loc.getCode().equals(req.code());
        if (codeChanged && locationRepository.existsByWarehouseIdAndCode(warehouseId, req.code())) {
            throw new BusinessException("Mã vị trí đã tồn tại trong kho");
        }
        mapper.updateLocation(req, loc);
        loc.setWarehouse(wh);
        return mapper.toLocationResponse(locationRepository.save(loc));
    }

    private Warehouse find(Long id) {
        return warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Kho", id));
    }
}
