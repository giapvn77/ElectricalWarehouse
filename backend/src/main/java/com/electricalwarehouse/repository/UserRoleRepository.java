package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.RoleCode;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);

    boolean existsByUserIdAndRoleCode(Long userId, RoleCode code);

    @Modifying
    void deleteByUserId(Long userId);
}
