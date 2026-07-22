package com.nexus.gateway.domain;

import com.nexus.gateway.provider.PaymentProvider;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

    @Id
    private String id;

    @Column(nullable = false)
    private String paymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    @Column(nullable = false)
    private String status;

    private String providerTransactionId;

    private String declineCode;

    private Boolean retryable;

    @Column(nullable = false)
    private Instant createdAt;

    protected PaymentAttempt() {}

    public PaymentAttempt(String id, String paymentIntentId, PaymentProvider provider,
                          String status, String providerTransactionId, String declineCode, Boolean retryable) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.provider = provider;
        this.status = status;
        this.providerTransactionId = providerTransactionId;
        this.declineCode = declineCode;
        this.retryable = retryable;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public PaymentProvider getProvider() { return provider; }
    public String getStatus() { return status; }
    public String getProviderTransactionId() { return providerTransactionId; }
    public String getDeclineCode() { return declineCode; }
    public Boolean getRetryable() { return retryable; }
    public Instant getCreatedAt() { return createdAt; }
}
