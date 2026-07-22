package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallets_merchant", columnList = "merchant_id"),
        @Index(name = "idx_wallets_merchant_currency", columnList = "merchant_id,currency")
})
public class Wallet {
    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "merchant_id", length = 64)
    private String merchantId;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "balance_cents", nullable = false)
    private int balanceCents;

    @Column(name = "pending_balance_cents", nullable = false)
    private int pendingBalanceCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status = WalletStatus.active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public int getBalanceCents() { return balanceCents; }
    public void setBalanceCents(int balanceCents) { this.balanceCents = balanceCents; }
    public int getPendingBalanceCents() { return pendingBalanceCents; }
    public void setPendingBalanceCents(int pendingBalanceCents) { this.pendingBalanceCents = pendingBalanceCents; }
    public WalletStatus getStatus() { return status; }
    public void setStatus(WalletStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
