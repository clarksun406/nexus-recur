package com.nexus.gateway.provider;

public record RefundResult(Status status, String providerRefundId) {

    public enum Status {
        SUCCEEDED, FAILED
    }

    public static RefundResult success(String providerRefundId) {
        return new RefundResult(Status.SUCCEEDED, providerRefundId);
    }

    public static RefundResult failure() {
        return new RefundResult(Status.FAILED, null);
    }
}
