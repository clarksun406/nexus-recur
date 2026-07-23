package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tax_rates", indexes = {
        @Index(name = "idx_tax_country", columnList = "country"),
        @Index(name = "idx_tax_jurisdiction", columnList = "jurisdiction")
})
public class TaxRate {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 64)
    private String jurisdiction;
    @Column(nullable = false, length = 2)
    private String country;
    @Column(length = 64)
    private String state;
    @Column(nullable = false)
    private int percentage;
    @Column(length = 64)
    private String displayName;
    private boolean inclusive;
    @Column(length = 32)
    private String taxCategory;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaxRateStatus status = TaxRateStatus.active;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJurisdiction() { return jurisdiction; }
    public void setJurisdiction(String jurisdiction) { this.jurisdiction = jurisdiction; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isInclusive() { return inclusive; }
    public void setInclusive(boolean inclusive) { this.inclusive = inclusive; }
    public String getTaxCategory() { return taxCategory; }
    public void setTaxCategory(String taxCategory) { this.taxCategory = taxCategory; }
    public TaxRateStatus getStatus() { return status; }
    public void setStatus(TaxRateStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
