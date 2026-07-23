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
@Table(name = "license_keys", indexes = {
        @Index(name = "idx_license_key", columnList = "licenseKey", unique = true),
        @Index(name = "idx_license_sub", columnList = "subscriptionId"),
        @Index(name = "idx_license_merchant", columnList = "merchantId")
})
public class LicenseKey {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 64, unique = true)
    private String licenseKey;
    @Column(nullable = false, length = 64)
    private String merchantId;
    @Column(length = 32)
    private String subscriptionId;
    @Column(length = 32)
    private String planId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LicenseStatus status = LicenseStatus.active;
    @Column(length = 128)
    private String deviceFingerprint;
    private int maxActivations = 1;
    private int currentActivations;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastValidatedAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt;

    @PreUpdate
    public void onUpdate() { this.updatedAt = OffsetDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public LicenseStatus getStatus() { return status; }
    public void setStatus(LicenseStatus status) { this.status = status; }
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    public int getMaxActivations() { return maxActivations; }
    public void setMaxActivations(int maxActivations) { this.maxActivations = maxActivations; }
    public int getCurrentActivations() { return currentActivations; }
    public void setCurrentActivations(int currentActivations) { this.currentActivations = currentActivations; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public OffsetDateTime getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(OffsetDateTime lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
