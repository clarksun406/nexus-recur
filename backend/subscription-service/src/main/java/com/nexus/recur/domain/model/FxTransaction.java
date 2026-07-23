package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fx_transactions", indexes = {
        @Index(name = "idx_fx_merchant", columnList = "merchantId"),
        @Index(name = "idx_fx_wallet", columnList = "sourceWalletId")
})
public class FxTransaction {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 64)
    private String merchantId;
    @Column(nullable = false, length = 32)
    private String sourceWalletId;
    @Column(nullable = false, length = 32)
    private String targetWalletId;
    @Column(nullable = false, length = 3)
    private String sourceCurrency;
    @Column(nullable = false, length = 3)
    private String targetCurrency;
    @Column(nullable = false)
    private long sourceAmountCents;
    @Column(nullable = false)
    private long targetAmountCents;
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal exchangeRate;
    @Column(nullable = false)
    private int spreadBps;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FxStatus status = FxStatus.pending;
    @Column(length = 256)
    private String failureReason;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime completedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getSourceWalletId() { return sourceWalletId; }
    public void setSourceWalletId(String sourceWalletId) { this.sourceWalletId = sourceWalletId; }
    public String getTargetWalletId() { return targetWalletId; }
    public void setTargetWalletId(String targetWalletId) { this.targetWalletId = targetWalletId; }
    public String getSourceCurrency() { return sourceCurrency; }
    public void setSourceCurrency(String sourceCurrency) { this.sourceCurrency = sourceCurrency; }
    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }
    public long getSourceAmountCents() { return sourceAmountCents; }
    public void setSourceAmountCents(long sourceAmountCents) { this.sourceAmountCents = sourceAmountCents; }
    public long getTargetAmountCents() { return targetAmountCents; }
    public void setTargetAmountCents(long targetAmountCents) { this.targetAmountCents = targetAmountCents; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public int getSpreadBps() { return spreadBps; }
    public void setSpreadBps(int spreadBps) { this.spreadBps = spreadBps; }
    public FxStatus getStatus() { return status; }
    public void setStatus(FxStatus status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
