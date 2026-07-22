package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_id", columnList = "keyId"),
        @Index(name = "idx_api_key_user", columnList = "userId")
})
public class ApiKey {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String keyId;
    @Column(nullable = false, length = 100)
    private String keyHash;
    @Column(nullable = false, length = 16)
    private String keyPrefix;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ApiKeyScope scope = ApiKeyScope.full_access;
    @Column(nullable = false, length = 64)
    private String userId;
    @Column(length = 64)
    private String merchantId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApiKeyStatus status = ApiKeyStatus.active;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime lastUsedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    public ApiKeyScope getScope() { return scope; }
    public void setScope(ApiKeyScope scope) { this.scope = scope; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public ApiKeyStatus getStatus() { return status; }
    public void setStatus(ApiKeyStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(OffsetDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
