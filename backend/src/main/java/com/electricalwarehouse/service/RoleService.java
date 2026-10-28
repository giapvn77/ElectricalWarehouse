package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.RolePermissionRequest;
import com.electricalwarehouse.dto.response.PermissionResponse;
import com.electricalwarehouse.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getRoles();

    List<PermissionResponse> getPermissions();

    RoleResponse updatePermissions(Long roleId, RolePermissionRequest request);
}
