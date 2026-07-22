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
}
