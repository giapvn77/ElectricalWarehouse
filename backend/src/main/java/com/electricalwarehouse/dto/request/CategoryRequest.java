package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank String name,
        Long parentId,
        Boolean active
) {}
