package com.nexus.recur.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

public final class LicenseDtos {
    private LicenseDtos() {}

    public record GenerateLicenseRequest(
            @NotBlank String merchantId,
            @NotBlank String planId,
            String subscriptionId,
            int maxActivations,
            Integer expiryDays
    ) {}

    public record ValidateLicenseRequest(
            @NotBlank String licenseKey,
            @NotBlank String deviceFingerprint
    ) {}

    public record LicenseResponse(
            String id,
            String licenseKey,
            String merchantId,
            String subscriptionId,
            String planId,
            String status,
            String deviceFingerprint,
            int maxActivations,
            int currentActivations,
            OffsetDateTime expiresAt,
            OffsetDateTime lastValidatedAt,
            OffsetDateTime createdAt
    ) {}
}
