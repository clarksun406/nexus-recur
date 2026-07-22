package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_invoices",
        uniqueConstraints = @UniqueConstraint(name = "uk_sub_period", columnNames = {"subscriptionId", "periodStart", "periodEnd"}),
        indexes = @Index(name = "idx_subscription", columnList = "subscriptionId"))
public class SubscriptionInvoice {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String subscriptionId;
    @Column(length = 64)
    private String externalTransactionId;
    @Column(nullable = false)
    private OffsetDateTime periodStart;
    @Column(nullable = false)
    private OffsetDateTime periodEnd;
    @Column(nullable = false)
    private int amountCents;
    @Column(length = 3, nullable = false)
    private String currency = "USD";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InvoiceStatus status = InvoiceStatus.pending;
    @Column(length = 32)
    private String paymentMethod;
    private OffsetDateTime paidAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getExternalTransactionId() { return externalTransactionId; }
    public void setExternalTransactionId(String externalTransactionId) { this.externalTransactionId = externalTransactionId; }
    public OffsetDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(OffsetDateTime periodStart) { this.periodStart = periodStart; }
    public OffsetDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(OffsetDateTime periodEnd) { this.periodEnd = periodEnd; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
