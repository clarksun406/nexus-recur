package com.nexus.gateway.provider;

public record RefundRequest(String providerTransactionId, long amountCents, String currency, String reason) {}
