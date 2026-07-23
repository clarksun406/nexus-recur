package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PreUpdate;
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
    private Integer subtotalCents;
    private Integer taxAmountCents;
    private Integer totalCents;
    private OffsetDateTime dueDate;
    @Column(length = 32)
    private String invoiceNumber;
    @Column(length = 64)
    private String paymentIntentId;
    @Column(length = 64)
    private String merchantId;
    @Column(length = 32)
    private String customerId;
    private Integer discountAmountCents;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

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
    public Integer getSubtotalCents() { return subtotalCents; }
    public void setSubtotalCents(Integer subtotalCents) { this.subtotalCents = subtotalCents; }
    public Integer getTaxAmountCents() { return taxAmountCents; }
    public void setTaxAmountCents(Integer taxAmountCents) { this.taxAmountCents = taxAmountCents; }
    public Integer getTotalCents() { return totalCents; }
    public void setTotalCents(Integer totalCents) { this.totalCents = totalCents; }
    public OffsetDateTime getDueDate() { return dueDate; }
    public void setDueDate(OffsetDateTime dueDate) { this.dueDate = dueDate; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public Integer getDiscountAmountCents() { return discountAmountCents; }
    public void setDiscountAmountCents(Integer discountAmountCents) { this.discountAmountCents = discountAmountCents; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
