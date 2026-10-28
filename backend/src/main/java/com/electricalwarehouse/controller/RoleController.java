package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.RolePermissionRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PermissionResponse;
import com.electricalwarehouse.dto.response.RoleResponse;
import com.electricalwarehouse.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.ok(roleService.getRoles());
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> permissions() {
        return ApiResponse.ok(roleService.getPermissions());
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<RoleResponse> updatePermissions(@PathVariable Long id,
                                                       @Valid @RequestBody RolePermissionRequest request) {
        return ApiResponse.ok(roleService.updatePermissions(id, request));
    }
}
