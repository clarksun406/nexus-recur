package com.nexus.recur.domain.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "routing_rules", indexes = {
        @Index(name = "idx_routing_merchant", columnList = "merchantId"),
        @Index(name = "idx_routing_provider", columnList = "providerName")
})
public class RoutingRule {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false, length = 64)
    private String merchantId;

    @Column(nullable = false, length = 64)
    private String providerName;

    @Column(nullable = false)
    private int priority;

    @Column(length = 3)
    private String currency;

    private Long minAmountCents;

    private Long maxAmountCents;

    @Column(length = 64)
    private String region;

    @Column(nullable = false)
    private int weight = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private RoutingStrategy strategy = RoutingStrategy.priority;

    private double successRate = 0.95;

    private double costPercentage = 2.9;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getMinAmountCents() { return minAmountCents; }
    public void setMinAmountCents(Long minAmountCents) { this.minAmountCents = minAmountCents; }
    public Long getMaxAmountCents() { return maxAmountCents; }
    public void setMaxAmountCents(Long maxAmountCents) { this.maxAmountCents = maxAmountCents; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public RoutingStrategy getStrategy() { return strategy; }
    public void setStrategy(RoutingStrategy strategy) { this.strategy = strategy; }
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public double getCostPercentage() { return costPercentage; }
    public void setCostPercentage(double costPercentage) { this.costPercentage = costPercentage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
