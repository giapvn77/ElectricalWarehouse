package com.electricalwarehouse.security;

import com.electricalwarehouse.entity.Permission;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.UserRole;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPrincipal loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return toPrincipal(user);
    }

    @Transactional(readOnly = true)
    public UserPrincipal toPrincipal(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        Set<RoleCode> roles = userRoles.stream().map(ur -> ur.getRole().getCode()).collect(Collectors.toSet());
        Set<String> permissions = userRoles.stream()
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        Long warehouseId = user.getWarehouse() != null ? user.getWarehouse().getId() : null;
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(), user.isActive(),
                warehouseId, roles, permissions);
    }
}
