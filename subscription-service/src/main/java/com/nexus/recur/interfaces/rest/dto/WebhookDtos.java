package com.nexus.recur.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public final class WebhookDtos {
    private WebhookDtos() {}

    public record SubscriptionWebhookPayload(
            String eventId,
            String eventType,
            String subscriptionId,
            String externalSubId,
            String externalCustomerId,
            String externalTransactionId,
            String planId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            Map<String, Object> metadata
    ) {}
}
