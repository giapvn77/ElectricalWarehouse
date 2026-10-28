package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.RoleCode;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(RoleCode code);
}
