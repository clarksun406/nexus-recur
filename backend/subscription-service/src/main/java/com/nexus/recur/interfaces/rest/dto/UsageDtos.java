package com.nexus.recur.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class UsageDtos {
    public record ReportUsageRequest(
            @NotBlank String subscriptionId,
            String planId,
            long quantity,
            String unitName,
            @NotBlank String idempotencyKey,
            OffsetDateTime recordedAt) {}

    public record UsageRecordResponse(
            String id, String subscriptionId, String planId,
            long quantity, String unitName, String idempotencyKey,
            OffsetDateTime recordedAt, OffsetDateTime createdAt) {}
}
