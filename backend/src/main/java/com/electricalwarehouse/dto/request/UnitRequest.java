package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank String name
) {}
