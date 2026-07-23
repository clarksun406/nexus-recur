package com.nexus.recur.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_settlement_merchant", columnList = "merchantId"),
        @Index(name = "idx_settlement_status", columnList = "status")
})
public class Settlement {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 64)
    private String merchantId;

    @Column(nullable = false, length = 32)
    private String walletId;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(length = 256)
    private String bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SettlementStatus status;

    @Column(length = 64)
    private String approvedBy;

    private OffsetDateTime approvedAt;

    @Column(columnDefinition = "text")
    private String backgroundRefs;

    @Column(length = 256)
    private String rejectionReason;

    @Column(precision = 18, scale = 8)
    private BigDecimal exchangeRate;

    private Long feeAmountCents;

    private Long netAmountCents;

    private OffsetDateTime expectedArrivalAt;

    @Column(length = 64)
    private String referenceNumber;

    @Column(length = 64)
    private String initiatedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ComplianceStatus complianceStatus;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime completedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public SettlementStatus getStatus() { return status; }
    public void setStatus(SettlementStatus status) { this.status = status; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getBackgroundRefs() { return backgroundRefs; }
    public void setBackgroundRefs(String backgroundRefs) { this.backgroundRefs = backgroundRefs; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public Long getFeeAmountCents() { return feeAmountCents; }
    public void setFeeAmountCents(Long feeAmountCents) { this.feeAmountCents = feeAmountCents; }
    public Long getNetAmountCents() { return netAmountCents; }
    public void setNetAmountCents(Long netAmountCents) { this.netAmountCents = netAmountCents; }
    public OffsetDateTime getExpectedArrivalAt() { return expectedArrivalAt; }
    public void setExpectedArrivalAt(OffsetDateTime expectedArrivalAt) { this.expectedArrivalAt = expectedArrivalAt; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getInitiatedBy() { return initiatedBy; }
    public void setInitiatedBy(String initiatedBy) { this.initiatedBy = initiatedBy; }
    public ComplianceStatus getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(ComplianceStatus complianceStatus) { this.complianceStatus = complianceStatus; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
