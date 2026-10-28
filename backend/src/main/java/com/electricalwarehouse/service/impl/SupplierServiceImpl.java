package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.PartnerRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.PartnerResponse;
import com.electricalwarehouse.entity.Supplier;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.SupplierRepository;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;
    private final AuditService auditService;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<PartnerResponse> search(String keyword, Pageable pageable) {
        return PagedResult.of(repository.search(InventoryServiceImpl.blankToNull(keyword), pageable),
                mapper::toPartnerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponse get(Long id) {
        return mapper.toPartnerResponse(find(id));
    }

    @Override
    @Transactional
    public PartnerResponse create(PartnerRequest req) {
        if (repository.existsByCode(req.code())) {
            throw new BusinessException("Mã nhà cung cấp đã tồn tại");
        }
        Supplier entity = new Supplier();
        mapper.updateSupplier(req, entity);
        repository.save(entity);
        auditService.log("CREATE", "SUPPLIER", entity.getId(), entity.getCode());
        return mapper.toPartnerResponse(entity);
    }

    @Override
    @Transactional
    public PartnerResponse update(Long id, PartnerRequest req) {
        Supplier entity = find(id);
        if (!entity.getCode().equals(req.code()) && repository.existsByCode(req.code())) {
            throw new BusinessException("Mã nhà cung cấp đã tồn tại");
        }
        mapper.updateSupplier(req, entity);
        auditService.log("UPDATE", "SUPPLIER", id, entity.getCode());
        return mapper.toPartnerResponse(entity);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        find(id).setActive(false);
        auditService.log("DEACTIVATE", "SUPPLIER", id, null);
    }

    private Supplier find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Nhà cung cấp", id));
    }
}
