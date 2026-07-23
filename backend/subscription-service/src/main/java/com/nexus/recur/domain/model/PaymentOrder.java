package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_orders", indexes = {
        @Index(name = "idx_po_merchant", columnList = "merchantId"),
        @Index(name = "idx_po_wallet", columnList = "walletId"),
        @Index(name = "idx_po_status", columnList = "status")
})
public class PaymentOrder {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 64)
    private String merchantId;
    @Column(nullable = false, length = 32)
    private String walletId;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false)
    private long amountCents;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PayoutMethod method;
    @Column(nullable = false)
    private String beneficiaryName;
    @Column(nullable = false)
    private String beneficiaryAccount;
    private String beneficiaryBank;
    @Column(length = 3)
    private String beneficiaryCountry;
    @Column(columnDefinition = "text")
    private String purpose;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private PaymentOrderStatus status = PaymentOrderStatus.pending_screening;
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private SanctionsResult sanctionsResult;
    private OffsetDateTime sanctionsCheckedAt;
    @Column(length = 64)
    private String approvedBy;
    private OffsetDateTime approvedAt;
    @Column(length = 64)
    private String referenceNumber;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt;

    @PreUpdate
    public void onUpdate() { this.updatedAt = OffsetDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getWalletId() { return walletId; }
    public void setWalletId(String walletId) { this.walletId = walletId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }
    public PayoutMethod getMethod() { return method; }
    public void setMethod(PayoutMethod method) { this.method = method; }
    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
    public String getBeneficiaryAccount() { return beneficiaryAccount; }
    public void setBeneficiaryAccount(String beneficiaryAccount) { this.beneficiaryAccount = beneficiaryAccount; }
    public String getBeneficiaryBank() { return beneficiaryBank; }
    public void setBeneficiaryBank(String beneficiaryBank) { this.beneficiaryBank = beneficiaryBank; }
    public String getBeneficiaryCountry() { return beneficiaryCountry; }
    public void setBeneficiaryCountry(String beneficiaryCountry) { this.beneficiaryCountry = beneficiaryCountry; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public PaymentOrderStatus getStatus() { return status; }
    public void setStatus(PaymentOrderStatus status) { this.status = status; }
    public SanctionsResult getSanctionsResult() { return sanctionsResult; }
    public void setSanctionsResult(SanctionsResult sanctionsResult) { this.sanctionsResult = sanctionsResult; }
    public OffsetDateTime getSanctionsCheckedAt() { return sanctionsCheckedAt; }
    public void setSanctionsCheckedAt(OffsetDateTime sanctionsCheckedAt) { this.sanctionsCheckedAt = sanctionsCheckedAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
