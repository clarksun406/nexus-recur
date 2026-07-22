package com.nexus.gateway.provider;

public record ChargeResult(Status status, String providerTransactionId, String declineCode, boolean retryable) {

    public enum Status {
        SUCCEEDED, FAILED, REQUIRES_ACTION
    }

    public static ChargeResult success(String providerTransactionId) {
        return new ChargeResult(Status.SUCCEEDED, providerTransactionId, null, false);
    }

    public static ChargeResult failure(String declineCode, boolean retryable) {
        return new ChargeResult(Status.FAILED, null, declineCode, retryable);
    }
}
