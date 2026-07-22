package com.nexus.gateway.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    private String id;

    @Column(nullable = false)
    private String paymentIntentId;

    private String providerRefundId;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency;

    private String reason;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    protected Refund() {}

    public Refund(String id, String paymentIntentId, long amountCents, String currency, String reason) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.reason = reason;
        this.status = "pending";
        this.createdAt = Instant.now();
    }

    public void markSucceeded(String providerRefundId) {
        this.status = "succeeded";
        this.providerRefundId = providerRefundId;
    }

    public void markFailed() {
        this.status = "failed";
    }

    public String getId() { return id; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public String getProviderRefundId() { return providerRefundId; }
    public long getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
