package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ScanSerialRequest(
        @NotNull Long detailId,
        @NotEmpty List<@NotBlank String> serialNumbers
) {}
