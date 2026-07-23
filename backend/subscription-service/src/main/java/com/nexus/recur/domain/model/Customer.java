package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_merchant", columnList = "merchantId"),
        @Index(name = "idx_customers_email", columnList = "email")
})
public class Customer {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 64)
    private String merchantId;
    @Column(length = 64)
    private String externalCustomerId;
    @Column(length = 128)
    private String email;
    @Column(length = 128)
    private String name;
    @Column(length = 32)
    private String phone;
    @Column(columnDefinition = "text")
    private String addressJson;
    @Column(length = 64)
    private String taxId;
    @Column(columnDefinition = "text")
    private String metadataJson;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getExternalCustomerId() { return externalCustomerId; }
    public void setExternalCustomerId(String externalCustomerId) { this.externalCustomerId = externalCustomerId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddressJson() { return addressJson; }
    public void setAddressJson(String addressJson) { this.addressJson = addressJson; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
