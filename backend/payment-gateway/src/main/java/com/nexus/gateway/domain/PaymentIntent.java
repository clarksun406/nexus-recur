package com.nexus.gateway.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "payment_intents")
public class PaymentIntent {

    @Id
    private String id;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency;

    private String description;

    @Column(nullable = false)
    private String status;

    private String providerTransactionId;

    @Enumerated(EnumType.STRING)
    private com.nexus.gateway.provider.PaymentProvider provider;

    private String declineCode;

    private Boolean retryable;

    private int attemptCount;

    private int maxAttempts;

    @Column(length = 2048)
    private String metadata;

    private String paymentMethodId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected PaymentIntent() {}

    public PaymentIntent(String id, String merchantId, long amountCents, String currency,
                         String description, String metadata, String paymentMethodId, int maxAttempts) {
        this.id = id;
        this.merchantId = merchantId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.description = description;
        this.metadata = metadata;
        this.paymentMethodId = paymentMethodId;
        this.maxAttempts = maxAttempts;
        this.status = "requires_confirmation";
        this.attemptCount = 0;
        this.createdAt = Instant.now();
    }

    public void markProcessing() {
        this.status = "processing";
        this.updatedAt = Instant.now();
    }

    public void markSucceeded(String providerTransactionId, com.nexus.gateway.provider.PaymentProvider provider) {
        this.status = "succeeded";
        this.providerTransactionId = providerTransactionId;
        this.provider = provider;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String declineCode, boolean retryable, com.nexus.gateway.provider.PaymentProvider provider) {
        this.status = "failed";
        this.declineCode = declineCode;
        this.retryable = retryable;
        this.provider = provider;
        this.updatedAt = Instant.now();
    }

    public void markCanceled() {
        this.status = "canceled";
        this.updatedAt = Instant.now();
    }

    public void incrementAttempts() {
        this.attemptCount++;
    }

    public String getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public long getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getProviderTransactionId() { return providerTransactionId; }
    public com.nexus.gateway.provider.PaymentProvider getProvider() { return provider; }
    public String getDeclineCode() { return declineCode; }
    public Boolean getRetryable() { return retryable; }
    public int getAttemptCount() { return attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public String getMetadata() { return metadata; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
