package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StorageLocationRequest(
        @NotBlank @Size(max = 30) String code,
        String zone,
        String description,
        Boolean active
) {}
