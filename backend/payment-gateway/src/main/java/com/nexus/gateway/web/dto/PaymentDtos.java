package com.nexus.gateway.web.dto;

import java.time.Instant;
import java.util.List;

public class PaymentDtos {

    public record CreateIntentRequest(
            String merchantId,
            long amountCents,
            String currency,
            String description,
            String metadata,
            String paymentMethodId
    ) {}

    public record IntentResponse(
            String id,
            String merchantId,
            long amountCents,
            String currency,
            String status,
            String providerTransactionId,
            String declineCode,
            int attemptCount,
            String metadata,
            Instant createdAt
    ) {}

    public record ConfirmResponse(
            String id,
            String status,
            String providerTransactionId,
            String declineCode,
            Boolean retryable,
            List<AttemptResponse> attempts
    ) {}

    public record AttemptResponse(
            String provider,
            String status,
            String declineCode,
            Boolean retryable,
            Instant createdAt
    ) {}

    public record CreateRefundRequest(
            String paymentIntentId,
            long amountCents,
            String currency,
            String reason
    ) {}

    public record RefundResponse(
            String id,
            String paymentIntentId,
            long amountCents,
            String currency,
            String status,
            String providerRefundId,
            Instant createdAt
    ) {}
}
