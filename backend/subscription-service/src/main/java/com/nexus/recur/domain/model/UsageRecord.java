package com.nexus.recur.domain.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usage_records", indexes = {
        @Index(name = "idx_usage_subscription", columnList = "subscriptionId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_usage_idempotency", columnNames = "idempotencyKey")
})
public class UsageRecord {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String subscriptionId;
    @Column(length = 32)
    private String planId;
    @Column(nullable = false)
    private long quantity;
    @Column(length = 32)
    private String unitName;
    @Column(length = 64)
    private String idempotencyKey;
    @Column(nullable = false)
    private OffsetDateTime recordedAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public OffsetDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(OffsetDateTime recordedAt) { this.recordedAt = recordedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
