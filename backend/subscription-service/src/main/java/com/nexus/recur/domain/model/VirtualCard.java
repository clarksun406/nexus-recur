package com.nexus.recur.domain.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "virtual_cards", indexes = {
        @Index(name = "idx_vcard_merchant", columnList = "merchantId"),
        @Index(name = "idx_vcard_customer", columnList = "customerId"),
        @Index(name = "idx_vcard_status", columnList = "status")
})
public class VirtualCard {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 64)
    private String merchantId;

    @Column(length = 32)
    private String customerId;

    @Column(nullable = false, length = 64)
    private String cardToken;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(nullable = false)
    private int expMonth;

    @Column(nullable = false)
    private int expYear;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private long spendingLimitCents;

    @Column(nullable = false)
    private long spentCents;

    @Column(length = 128)
    private String label;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private VirtualCardStatus status = VirtualCardStatus.active;

    @Column(nullable = false)
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    private OffsetDateTime expiresAt;

    private OffsetDateTime frozenAt;

    private OffsetDateTime closedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCardToken() { return cardToken; }
    public void setCardToken(String cardToken) { this.cardToken = cardToken; }
    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }
    public int getExpMonth() { return expMonth; }
    public void setExpMonth(int expMonth) { this.expMonth = expMonth; }
    public int getExpYear() { return expYear; }
    public void setExpYear(int expYear) { this.expYear = expYear; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public long getSpendingLimitCents() { return spendingLimitCents; }
    public void setSpendingLimitCents(long spendingLimitCents) { this.spendingLimitCents = spendingLimitCents; }
    public long getSpentCents() { return spentCents; }
    public void setSpentCents(long spentCents) { this.spentCents = spentCents; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public VirtualCardStatus getStatus() { return status; }
    public void setStatus(VirtualCardStatus status) { this.status = status; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(OffsetDateTime issuedAt) { this.issuedAt = issuedAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public OffsetDateTime getFrozenAt() { return frozenAt; }
    public void setFrozenAt(OffsetDateTime frozenAt) { this.frozenAt = frozenAt; }
    public OffsetDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(OffsetDateTime closedAt) { this.closedAt = closedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
