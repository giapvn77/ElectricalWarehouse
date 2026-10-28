package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.dto.request.UserRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.UserResponse;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.UserRole;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.exception.NotFoundException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.RoleRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.UserRoleRepository;
import com.electricalwarehouse.repository.UserSessionRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.security.CustomUserDetailsService;
import com.electricalwarehouse.security.UserPrincipal;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.Lookup;
import com.electricalwarehouse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSessionRepository sessionRepository;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final Lookup lookup;
    private final DTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResult<UserResponse> search(String keyword, Pageable pageable) {
        return PagedResult.of(userRepository.search(InventoryServiceImpl.blankToNull(keyword), pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public UserResponse create(UserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        if (!StringUtils.hasText(req.password())) {
            throw new BusinessException("Mật khẩu là bắt buộc khi tạo tài khoản");
        }
        User user = new User();
        user.setUsername(req.username().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        apply(user, req);
        userRepository.save(user);
        assignRoles(user, req);
        auditService.log("CREATE", "USER", user.getId(), "roles=" + req.roles());
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest req) {
        User user = find(id);
        if (!user.getUsername().equals(req.username()) && userRepository.existsByUsername(req.username())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        user.setUsername(req.username().trim());
        if (StringUtils.hasText(req.password())) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
            sessionRepository.revokeAllByUserId(user.getId());
        }
        apply(user, req);
        userRoleRepository.deleteByUserId(user.getId());
        userRoleRepository.flush();
        assignRoles(user, req);
        auditService.log("UPDATE", "USER", user.getId(), "roles=" + req.roles());
        return toResponse(user);
    }

    @Override
    @Transactional
    public void setActive(Long id, boolean active) {
        UserPrincipal me = CurrentUser.get();
        if (me.id().equals(id) && !active) {
            throw new BusinessException("Không thể tự khóa tài khoản của mình");
        }
        User user = find(id);
        user.setActive(active);
        if (!active) {
            sessionRepository.revokeAllByUserId(id);
        }
        auditService.log(active ? "ACTIVATE" : "DEACTIVATE", "USER", id, null);
    }

    private void apply(User user, UserRequest req) {
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setWarehouse(req.warehouseId() != null ? lookup.warehouse(req.warehouseId()) : null);
        if (req.active() != null) {
            user.setActive(req.active());
        }
    }

    private void assignRoles(User user, UserRequest req) {
        for (RoleCode code : req.roles()) {
            userRoleRepository.save(new UserRole(user,
                    roleRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Vai trò", code))));
        }
    }

    private UserResponse toResponse(User user) {
        UserPrincipal p = userDetailsService.toPrincipal(user);
        return mapper.toUserResponse(user, p.roles(), p.permissions());
    }

    private User find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Người dùng", id));
    }
}
