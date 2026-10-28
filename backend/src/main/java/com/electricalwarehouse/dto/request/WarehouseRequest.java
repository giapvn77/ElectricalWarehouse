package com.electricalwarehouse.dto.request;

import com.electricalwarehouse.entity.enums.WarehouseType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank String name,
        String address,
        @NotNull WarehouseType type,
        @Email String managerEmail,
        Boolean active
) {}
