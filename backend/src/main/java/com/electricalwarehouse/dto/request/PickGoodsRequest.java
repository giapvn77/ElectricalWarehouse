package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Serial-managed lines send serialNumbers (quantity is ignored); other lines send quantity. */
public record PickGoodsRequest(
        @NotNull Long detailId,
        @Min(0) int quantity,
        List<String> serialNumbers
) {}
