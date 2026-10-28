package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.DefectRegistrationRequest;
import com.electricalwarehouse.dto.request.DefectResolutionRequest;
import com.electricalwarehouse.dto.response.DefectiveGoodResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.entity.enums.DefectStatus;
import org.springframework.data.domain.Pageable;

/** Hàng lỗi: chuyển vào kho hàng lỗi, sau đó sửa chữa / trả NCC / tiêu hủy. */
public interface DefectiveService {

    PagedResult<DefectiveGoodResponse> search(DefectStatus status, Long warehouseId, Pageable pageable);

    DefectiveGoodResponse register(DefectRegistrationRequest request);

    DefectiveGoodResponse resolve(Long id, DefectResolutionRequest request);
}
