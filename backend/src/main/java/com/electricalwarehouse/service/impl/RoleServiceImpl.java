package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.RolePermissionRequest;
import com.electricalwarehouse.dto.response.PermissionResponse;
import com.electricalwarehouse.dto.response.RoleResponse;
import com.electricalwarehouse.entity.Permission;
import com.electricalwarehouse.entity.Role;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.PermissionRepository;
import com.electricalwarehouse.repository.RoleRepository;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(mapper::toRoleResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions() {
        return permissionRepository.findAll().stream().map(mapper::toPermissionResponse).toList();
    }

    @Override
    @Transactional
    public RoleResponse updatePermissions(Long roleId, RolePermissionRequest req) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new NotFoundException("Vai trò", roleId));
        if (role.getCode() == RoleCode.ADMIN) {
            throw new BusinessException("Không thể thay đổi quyền của vai trò ADMIN");
        }
        role.getPermissions().clear();
        for (String code : req.permissionCodes()) {
            Permission p = permissionRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Quyền", code));
            role.getPermissions().add(p);
        }
        auditService.log("UPDATE_PERMISSIONS", "ROLE", roleId, String.join(",", req.permissionCodes()));
        return mapper.toRoleResponse(role);
    }
}
