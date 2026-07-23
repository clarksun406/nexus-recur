package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.EntityStatus;

import java.time.OffsetDateTime;

public class LegalEntityDtos {

    public record CreateEntityRequest(
            String name,
            String registrationNumber,
            String country,
            String taxId,
            String addressJson,
            String bankAccountJson,
            String primaryContact,
            String primaryEmail,
            String baseCurrency
    ) {}

    public record UpdateEntityRequest(
            String name,
            String registrationNumber,
            String country,
            String taxId,
            String addressJson,
            String bankAccountJson,
            String primaryContact,
            String primaryEmail,
            String baseCurrency,
            EntityStatus status
    ) {}

    public record AssignMerchantRequest(
            String merchantId
    ) {}

    public record LegalEntityResponse(
            String id,
            String name,
            String registrationNumber,
            String country,
            String taxId,
            String addressJson,
            String bankAccountJson,
            EntityStatus status,
            String primaryContact,
            String primaryEmail,
            String baseCurrency,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}
}
