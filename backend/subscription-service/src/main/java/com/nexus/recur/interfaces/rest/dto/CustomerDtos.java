package com.nexus.recur.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class CustomerDtos {
    public record CreateCustomerRequest(
            @NotBlank String merchantId,
            String externalCustomerId,
            @NotBlank String email,
            String name,
            String phone,
            String addressJson,
            String taxId,
            String metadataJson) {}

    public record UpdateCustomerRequest(
            String email, String name, String phone,
            String addressJson, String taxId, String metadataJson) {}

    public record CustomerResponse(
            String id, String merchantId, String externalCustomerId,
            String email, String name, String phone,
            String addressJson, String taxId, String metadataJson,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
}
