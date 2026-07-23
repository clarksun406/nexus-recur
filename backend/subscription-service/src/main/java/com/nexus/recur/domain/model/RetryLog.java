package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retry_logs", indexes = {
        @Index(name = "idx_retry_subscription", columnList = "subscriptionId")
})
public class RetryLog {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String subscriptionId;
    @Column(length = 32)
    private String invoiceId;
    @Column(nullable = false)
    private int attemptNumber;
    @Column(nullable = false)
    private OffsetDateTime attemptedAt;
    @Column(length = 64)
    private String declineCode;
    private OffsetDateTime nextRetryAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    public OffsetDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(OffsetDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
    public String getDeclineCode() { return declineCode; }
    public void setDeclineCode(String declineCode) { this.declineCode = declineCode; }
    public OffsetDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(OffsetDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
