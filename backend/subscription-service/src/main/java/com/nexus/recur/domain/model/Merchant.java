package com.nexus.recur.domain.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 128)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private KycStatus kycStatus = KycStatus.none;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime kycApprovedAt;

    @Column(length = 128)
    private String companyName;

    @Column(length = 2)
    private String country;

    @Column(length = 32)
    private String businessType;

    @Column(length = 64)
    private String taxId;

    @Column(length = 32)
    private String phone;

    @Column(columnDefinition = "text")
    private String addressJson;

    @Column(length = 256)
    private String website;

    @Column(length = 64)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MerchantStatus status = MerchantStatus.active;

    @Column(length = 3)
    private String defaultCurrency;

    private OffsetDateTime kycSubmittedAt;

    @Column(length = 256)
    private String kycRejectedReason;

    @Column(length = 32)
    private String legalEntityId;

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public KycStatus getKycStatus() { return kycStatus; }
    public void setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getKycApprovedAt() { return kycApprovedAt; }
    public void setKycApprovedAt(OffsetDateTime kycApprovedAt) { this.kycApprovedAt = kycApprovedAt; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddressJson() { return addressJson; }
    public void setAddressJson(String addressJson) { this.addressJson = addressJson; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public MerchantStatus getStatus() { return status; }
    public void setStatus(MerchantStatus status) { this.status = status; }
    public String getDefaultCurrency() { return defaultCurrency; }
    public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
    public OffsetDateTime getKycSubmittedAt() { return kycSubmittedAt; }
    public void setKycSubmittedAt(OffsetDateTime kycSubmittedAt) { this.kycSubmittedAt = kycSubmittedAt; }
    public String getKycRejectedReason() { return kycRejectedReason; }
    public void setKycRejectedReason(String kycRejectedReason) { this.kycRejectedReason = kycRejectedReason; }
    public String getLegalEntityId() { return legalEntityId; }
    public void setLegalEntityId(String legalEntityId) { this.legalEntityId = legalEntityId; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
