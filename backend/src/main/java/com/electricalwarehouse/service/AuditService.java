package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.response.AuditLogResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.entity.AuditLog;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.AuditLogRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Records who did what to which record. Runs in the caller's transaction so failed actions leave no log. */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final DTOMapper mapper;

    @Transactional
    public void log(String action, String entityType, Long entityId, String detail) {
        AuditLog log = new AuditLog();
        UserPrincipal principal = CurrentUser.orNull();
        if (principal != null) {
            log.setUser(userRepository.getReferenceById(principal.id()));
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetail(detail);
        log.setIpAddress(clientIp());
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PagedResult<AuditLogResponse> search(String entityType, Long entityId, Pageable pageable) {
        return PagedResult.of(auditLogRepository.search(entityType, entityId, pageable), mapper::toAuditLogResponse);
    }

    private static String clientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getRemoteAddr();
    }
}
