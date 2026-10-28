package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.PartnerRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.PartnerResponse;
import com.electricalwarehouse.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService service;

    @GetMapping
    public ApiResponse<PagedResult<PartnerResponse>> search(@RequestParam(required = false) String keyword,
                                                            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(service.search(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnerResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<PartnerResponse> create(@Valid @RequestBody PartnerRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<PartnerResponse> update(@PathVariable Long id, @Valid @RequestBody PartnerRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOG_MANAGE')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ApiResponse.ok();
    }
}
