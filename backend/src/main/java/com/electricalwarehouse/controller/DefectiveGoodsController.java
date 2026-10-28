package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.DefectRegistrationRequest;
import com.electricalwarehouse.dto.request.DefectResolutionRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.DefectiveGoodResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.entity.enums.DefectStatus;
import com.electricalwarehouse.service.DefectiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/defects")
@RequiredArgsConstructor
public class DefectiveGoodsController {
    private final DefectiveService defectiveService;

    @GetMapping
    public ApiResponse<PagedResult<DefectiveGoodResponse>> search(@RequestParam(required = false) DefectStatus status,
                                                                  @RequestParam(required = false) Long warehouseId,
                                                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(defectiveService.search(status, warehouseId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DEFECT_MANAGE')")
    public ApiResponse<DefectiveGoodResponse> register(@Valid @RequestBody DefectRegistrationRequest request) {
        return ApiResponse.ok(defectiveService.register(request));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'QLK')")
    public ApiResponse<DefectiveGoodResponse> resolve(@PathVariable Long id,
                                                      @Valid @RequestBody DefectResolutionRequest request) {
        return ApiResponse.ok(defectiveService.resolve(id, request));
    }
}
