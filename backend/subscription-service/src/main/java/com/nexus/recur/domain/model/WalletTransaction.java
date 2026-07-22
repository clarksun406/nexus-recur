package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wt_wallet", columnList = "wallet_id"),
        @Index(name = "idx_wt_created", columnList = "created_at")
})
public class WalletTransaction {
    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "wallet_id", nullable = false, length = 32)
    private String walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTransactionType type;

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "reference_type", length = 32)
    private String referenceType;

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }
    public WalletTransactionType getType() { return type; }
    public void setType(WalletTransactionType type) { this.type = type; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
