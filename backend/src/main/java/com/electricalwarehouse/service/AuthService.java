package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.ChangePasswordRequest;
import com.electricalwarehouse.dto.request.LoginRequest;
import com.electricalwarehouse.dto.response.AuthResponse;
import com.electricalwarehouse.dto.response.UserResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);

    /** Rotates the refresh token: the old one is revoked and a new pair is issued. */
    AuthResponse refresh(String refreshToken, String ipAddress, String userAgent);

    void logout(String refreshToken);

    UserResponse me();

    void changePassword(ChangePasswordRequest request);
}
