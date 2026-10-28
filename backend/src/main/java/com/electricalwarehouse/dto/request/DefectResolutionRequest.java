package com.electricalwarehouse.dto.request;

import com.electricalwarehouse.entity.enums.DefectStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DefectResolutionRequest(
        @NotNull DefectStatus status,
        @Size(max = 1000) String note
) {}
