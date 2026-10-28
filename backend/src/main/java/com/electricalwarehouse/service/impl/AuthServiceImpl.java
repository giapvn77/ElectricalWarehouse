package com.electricalwarehouse.service.impl;

import com.electricalwarehouse.config.AppProperties;
import com.electricalwarehouse.dto.request.ChangePasswordRequest;
import com.electricalwarehouse.dto.request.LoginRequest;
import com.electricalwarehouse.dto.response.AuthResponse;
import com.electricalwarehouse.dto.response.UserResponse;
import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.UserSession;
import com.electricalwarehouse.exception.BusinessException;
import com.electricalwarehouse.mapper.DTOMapper;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.UserSessionRepository;
import com.electricalwarehouse.security.CurrentUser;
import com.electricalwarehouse.security.CustomUserDetailsService;
import com.electricalwarehouse.security.JwtTokenProvider;
import com.electricalwarehouse.security.UserPrincipal;
import com.electricalwarehouse.service.AuditService;
import com.electricalwarehouse.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final AppProperties props;
    private final DTOMapper mapper;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req, String ip, String userAgent) {
        User user = userRepository.findByUsername(req.username().trim())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        return issueTokens(user, ip, userAgent);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken, String ip, String userAgent) {
        UserSession session = sessionRepository.findByRefreshTokenHashAndRevokedFalse(hash(refreshToken))
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn"));
        session.setRevoked(true);
        User user = session.getUser();
        if (!user.isActive()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Tài khoản đã bị khóa");
        }
        return issueTokens(user, ip, userAgent);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByRefreshTokenHashAndRevokedFalse(hash(refreshToken)).ifPresent(s -> s.setRevoked(true));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me() {
        UserPrincipal p = CurrentUser.get();
        return mapper.toUserResponse(userRepository.findById(p.id()).orElseThrow(), p.roles(), p.permissions());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        User user = userRepository.findById(CurrentUser.id()).orElseThrow();
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        // Sign out every other device.
        sessionRepository.revokeAllByUserId(user.getId());
        auditService.log("CHANGE_PASSWORD", "USER", user.getId(), null);
    }

    private AuthResponse issueTokens(User user, String ip, String userAgent) {
        UserPrincipal principal = userDetailsService.toPrincipal(user);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenHash(hash(refreshToken));
        session.setExpiresAt(LocalDateTime.now().plusDays(props.jwt().refreshTokenDays()));
        session.setIpAddress(ip);
        session.setUserAgent(userAgent != null && userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent);
        sessionRepository.save(session);

        return new AuthResponse(tokenProvider.createAccessToken(principal), refreshToken,
                tokenProvider.getAccessTtlSeconds(),
                mapper.toUserResponse(user, principal.roles(), principal.permissions()));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
