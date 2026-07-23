package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.RefundStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public class RefundDtos {
    public record CreateRefundRequest(
            @NotBlank String invoiceId,
            String subscriptionId,
            @NotBlank String merchantId,
            int amountCents,
            @NotBlank String currency,
            String reason,
            String initiatedBy) {}

    public record ApproveRefundRequest(@NotBlank String approvedBy) {}

    public record RefundResponse(
            String id, String invoiceId, String subscriptionId,
            String merchantId, int amountCents, String currency,
            String reason, RefundStatus status,
            String initiatedBy, String approvedBy,
            OffsetDateTime processedAt, OffsetDateTime createdAt) {}
}
