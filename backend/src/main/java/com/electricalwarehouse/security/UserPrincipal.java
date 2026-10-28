package com.electricalwarehouse.security;

import com.electricalwarehouse.entity.enums.RoleCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** Authenticated user. Roles map to ROLE_xxx authorities, permissions to plain authorities. */
public record UserPrincipal(Long id, String username, String password, boolean active, Long warehouseId,
                            Set<RoleCode> roles, Set<String> permissions) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name())));
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        return authorities;
    }

    public boolean hasRole(RoleCode role) {
        return roles.contains(role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
