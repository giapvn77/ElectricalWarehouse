package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.PartnerRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.PartnerResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    PagedResult<PartnerResponse> search(String keyword, Pageable pageable);

    PartnerResponse get(Long id);

    PartnerResponse create(PartnerRequest request);

    PartnerResponse update(Long id, PartnerRequest request);

    void deactivate(Long id);
}
