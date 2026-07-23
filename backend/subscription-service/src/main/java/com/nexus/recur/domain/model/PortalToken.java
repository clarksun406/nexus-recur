package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "portal_tokens", indexes = {
        @Index(name = "idx_portal_customer", columnList = "customerId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_portal_token", columnNames = "token")
})
public class PortalToken {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String customerId;
    @Column(nullable = false, length = 64)
    private String token;
    @Column(nullable = false)
    private OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public OffsetDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(OffsetDateTime usedAt) { this.usedAt = usedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() { return OffsetDateTime.now().isAfter(expiresAt); }
    public boolean isUsed() { return usedAt != null; }
}
