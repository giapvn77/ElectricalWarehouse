package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.PartnerRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.PartnerResponse;
import com.electricalwarehouse.entity.Customer;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.CustomerRepository;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
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
            throw new BusinessException("Mã khách hàng đã tồn tại");
        }
        Customer entity = new Customer();
        mapper.updateCustomer(req, entity);
        repository.save(entity);
        auditService.log("CREATE", "CUSTOMER", entity.getId(), entity.getCode());
        return mapper.toPartnerResponse(entity);
    }

    @Override
    @Transactional
    public PartnerResponse update(Long id, PartnerRequest req) {
        Customer entity = find(id);
        if (!entity.getCode().equals(req.code()) && repository.existsByCode(req.code())) {
            throw new BusinessException("Mã khách hàng đã tồn tại");
        }
        mapper.updateCustomer(req, entity);
        auditService.log("UPDATE", "CUSTOMER", id, entity.getCode());
        return mapper.toPartnerResponse(entity);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        find(id).setActive(false);
        auditService.log("DEACTIVATE", "CUSTOMER", id, null);
    }

    private Customer find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Khách hàng", id));
    }
}
