package com.electricalwarehouse.dto.request;

import com.electricalwarehouse.entity.enums.RoleCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** password is required on create and optional on update. */
public record UserRequest(
        @NotBlank @Size(max = 50) String username,
        @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 150) String fullName,
        @Email String email,
        String phone,
        Long warehouseId,
        @NotEmpty Set<RoleCode> roles,
        Boolean active
) {}
