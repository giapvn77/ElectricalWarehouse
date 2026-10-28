package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.electricalwarehouse.entity.enums.RoleCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            select u from User u
            where :keyword is null
               or lower(u.username) like lower(concat('%', :keyword, '%'))
               or lower(u.fullName) like lower(concat('%', :keyword, '%'))
            """)
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select distinct ur.user from UserRole ur
            where ur.role.code in :codes and ur.user.active = true
              and (ur.user.warehouse is null or ur.user.warehouse.id = :warehouseId)
            """)
    List<User> findActiveByRolesForWarehouse(@Param("codes") Collection<RoleCode> codes,
                                             @Param("warehouseId") Long warehouseId);
}
