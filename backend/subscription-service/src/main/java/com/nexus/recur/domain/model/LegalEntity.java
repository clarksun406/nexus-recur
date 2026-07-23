package com.nexus.recur.domain.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "legal_entities", indexes = {
        @Index(name = "idx_entity_country", columnList = "country"),
        @Index(name = "idx_entity_status", columnList = "status")
})
public class LegalEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 64)
    private String registrationNumber;

    @Column(nullable = false, length = 3)
    private String country;

    @Column(length = 64)
    private String taxId;

    @Column(columnDefinition = "TEXT")
    private String addressJson;

    @Column(columnDefinition = "TEXT")
    private String bankAccountJson;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private EntityStatus status = EntityStatus.active;

    @Column(length = 128)
    private String primaryContact;

    @Column(length = 128)
    private String primaryEmail;

    @Column(length = 3)
    private String baseCurrency;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getAddressJson() { return addressJson; }
    public void setAddressJson(String addressJson) { this.addressJson = addressJson; }
    public String getBankAccountJson() { return bankAccountJson; }
    public void setBankAccountJson(String bankAccountJson) { this.bankAccountJson = bankAccountJson; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public String getPrimaryContact() { return primaryContact; }
    public void setPrimaryContact(String primaryContact) { this.primaryContact = primaryContact; }
    public String getPrimaryEmail() { return primaryEmail; }
    public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
