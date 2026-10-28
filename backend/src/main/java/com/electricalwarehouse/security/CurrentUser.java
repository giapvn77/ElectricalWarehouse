package com.electricalwarehouse.security;

import com.electricalwarehouse.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Access to the authenticated user from service code. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }
        return principal;
    }

    public static Long id() {
        return get().id();
    }

    /** Null when called outside a request (scheduled jobs). */
    public static UserPrincipal orNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof UserPrincipal p ? p : null;
    }
}
