package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.ApiKeyScope;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public final class ApiKeyDtos {
    private ApiKeyDtos() {}

    public record CreateApiKeyRequest(
            @NotBlank String userId,
            String merchantId,
            ApiKeyScope scope
    ) {}

    public record CreateApiKeyResponse(
            String id,
            String key,
            String keyPrefix,
            ApiKeyScope scope,
            String userId,
            String merchantId
    ) {}

    public record ApiKeyResponse(
            String id,
            String keyPrefix,
            ApiKeyScope scope,
            String userId,
            String merchantId,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime lastUsedAt
    ) {}
}
