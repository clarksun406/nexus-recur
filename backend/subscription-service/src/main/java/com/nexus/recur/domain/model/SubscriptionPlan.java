package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    @Column(length = 64)
    private String productId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BillingCycle billingCycle;
    @Column(nullable = false)
    private int priceCents;
    @Column(length = 3, nullable = false)
    private String currency = "USD";
    private int trialDays;
    @Column(columnDefinition = "text")
    private String featuresJson;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PlanStatus status = PlanStatus.draft;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BillingType billingType = BillingType.flat_rate;
    @Column(columnDefinition = "text")
    private String meteredConfigJson;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaxMode taxMode = TaxMode.none;
    @Column(length = 32)
    private String taxCategory;
    private boolean licenseEnabled;
    private int licenseInstanceLimit = 1;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public int getTrialDays() { return trialDays; }
    public void setTrialDays(int trialDays) { this.trialDays = trialDays; }
    public String getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(String featuresJson) { this.featuresJson = featuresJson; }
    public PlanStatus getStatus() { return status; }
    public void setStatus(PlanStatus status) { this.status = status; }
    public BillingType getBillingType() { return billingType; }
    public void setBillingType(BillingType billingType) { this.billingType = billingType; }
    public String getMeteredConfigJson() { return meteredConfigJson; }
    public void setMeteredConfigJson(String meteredConfigJson) { this.meteredConfigJson = meteredConfigJson; }
    public TaxMode getTaxMode() { return taxMode; }
    public void setTaxMode(TaxMode taxMode) { this.taxMode = taxMode; }
    public String getTaxCategory() { return taxCategory; }
    public void setTaxCategory(String taxCategory) { this.taxCategory = taxCategory; }
    public boolean isLicenseEnabled() { return licenseEnabled; }
    public void setLicenseEnabled(boolean licenseEnabled) { this.licenseEnabled = licenseEnabled; }
    public int getLicenseInstanceLimit() { return licenseInstanceLimit; }
    public void setLicenseInstanceLimit(int licenseInstanceLimit) { this.licenseInstanceLimit = licenseInstanceLimit; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
