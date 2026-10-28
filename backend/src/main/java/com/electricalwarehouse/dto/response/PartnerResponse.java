package com.electricalwarehouse.dto.response;

public record PartnerResponse(
        Long id,
        String code,
        String name,
        String taxCode,
        String phone,
        String email,
        String address,
        String contactPerson,
        boolean active
) {}
