package com.electricalwarehouse.config;

import com.electricalwarehouse.entity.Permission;
import com.electricalwarehouse.entity.Role;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.UserRole;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.repository.PermissionRepository;
import com.electricalwarehouse.repository.RoleRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Seeds roles, permissions and the first admin account. Safe to run on every start. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Map<RoleCode, String> ROLE_NAMES = new LinkedHashMap<>();

    static {
        ROLE_NAMES.put(RoleCode.ADMIN, "Quản trị hệ thống");
        ROLE_NAMES.put(RoleCode.QLK, "Quản lý kho");
        ROLE_NAMES.put(RoleCode.NVTM, "Nhân viên thu mua");
        ROLE_NAMES.put(RoleCode.NVK, "Nhân viên kho");
        ROLE_NAMES.put(RoleCode.NVBH, "Nhân viên bán hàng");
    }

    /** permission code -> roles that hold it (ADMIN implicitly holds everything). */
    private static final Map<String, Set<RoleCode>> PERMISSIONS = new LinkedHashMap<>();

    static {
        PERMISSIONS.put("CATALOG_MANAGE", Set.of(RoleCode.QLK, RoleCode.NVTM));
        PERMISSIONS.put("STOCK_IN_CREATE", Set.of(RoleCode.NVTM, RoleCode.NVK));
        PERMISSIONS.put("STOCK_IN_APPROVE", Set.of(RoleCode.QLK));
        PERMISSIONS.put("STOCK_OUT_CREATE", Set.of(RoleCode.NVBH, RoleCode.NVK));
        PERMISSIONS.put("STOCK_OUT_PICK", Set.of(RoleCode.NVK));
        PERMISSIONS.put("STOCK_OUT_APPROVE", Set.of(RoleCode.QLK));
        PERMISSIONS.put("INVENTORY_ADJUST", Set.of(RoleCode.QLK, RoleCode.NVK));
        PERMISSIONS.put("TRANSFER_MANAGE", Set.of(RoleCode.QLK, RoleCode.NVK));
        PERMISSIONS.put("DEFECT_MANAGE", Set.of(RoleCode.QLK, RoleCode.NVK, RoleCode.NVBH));
        PERMISSIONS.put("REPORT_VIEW", Set.of(RoleCode.QLK, RoleCode.NVTM, RoleCode.NVBH));
        PERMISSIONS.put("USER_MANAGE", Set.of());
    }

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PERMISSIONS.keySet().forEach(code -> permissionRepository.findByCode(code).orElseGet(() -> {
            Permission p = new Permission();
            p.setCode(code);
            p.setName(code);
            p.setModule(code.split("_")[0]);
            return permissionRepository.save(p);
        }));

        ROLE_NAMES.forEach((code, name) -> {
            Role role = roleRepository.findByCode(code).orElseGet(Role::new);
            role.setCode(code);
            role.setName(name);
            PERMISSIONS.forEach((perm, holders) -> {
                if (code == RoleCode.ADMIN || holders.contains(code)) {
                    permissionRepository.findByCode(perm).ifPresent(role.getPermissions()::add);
                }
            });
            roleRepository.save(role);
        });

        seedAdmin();
    }

    private void seedAdmin() {
        String username = props.bootstrap().adminUsername();
        if (userRepository.existsByUsername(username)) {
            return;
        }
        String password = props.bootstrap().adminPassword();
        if (!StringUtils.hasText(password)) {
            log.warn("No admin account exists. Set APP_ADMIN_PASSWORD to create '{}' on next start.", username);
            return;
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setFullName("Administrator");
        admin.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(admin);
        userRoleRepository.save(new UserRole(admin, roleRepository.findByCode(RoleCode.ADMIN).orElseThrow()));
        log.info("Created admin account '{}'", username);
    }
}
