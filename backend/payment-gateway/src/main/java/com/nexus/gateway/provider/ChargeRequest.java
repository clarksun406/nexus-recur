package com.nexus.gateway.provider;

public record ChargeRequest(
        String paymentIntentId,
        long amountCents,
        String currency,
        String paymentMethodId,
        String idempotencyKey,
        String description
) {}
