package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refunds_merchant", columnList = "merchantId"),
        @Index(name = "idx_refunds_invoice", columnList = "invoiceId")
})
public class Refund {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String invoiceId;
    @Column(length = 32)
    private String subscriptionId;
    @Column(nullable = false, length = 64)
    private String merchantId;
    @Column(nullable = false)
    private int amountCents;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(length = 256)
    private String reason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RefundStatus status = RefundStatus.pending;
    @Column(length = 64)
    private String initiatedBy;
    @Column(length = 64)
    private String approvedBy;
    private OffsetDateTime processedAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }
    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(OffsetDateTime processedAt) { this.processedAt = processedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
