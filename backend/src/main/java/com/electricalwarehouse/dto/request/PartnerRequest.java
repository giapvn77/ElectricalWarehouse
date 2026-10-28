package com.electricalwarehouse.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Shared payload for suppliers and customers. */
public record PartnerRequest(
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 200) String name,
        String taxCode,
        String phone,
        @Email String email,
        String address,
        String contactPerson,
        Boolean active
) {}
