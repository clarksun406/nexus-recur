package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_period_end", columnList = "currentPeriodEnd")
})
public class Subscription {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 64)
    private String userId;
    @Column(nullable = false, length = 32)
    private String planId;
    @Column(length = 64)
    private String externalSubId;
    @Column(length = 64)
    private String externalCustomerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SubscriptionStatus status = SubscriptionStatus.pending;
    private OffsetDateTime currentPeriodStart;
    private OffsetDateTime currentPeriodEnd;
    private OffsetDateTime trialEndAt;
    private boolean cancelAtPeriodEnd;
    private OffsetDateTime canceledAt;
    private String cancelReason;
    private OffsetDateTime pausedAt;
    @Column(columnDefinition = "text")
    private String metadataJson;
    private int retryCount;
    @Column(length = 64)
    private String lastDeclineCode;
    @Column(length = 64)
    private String paymentMethodId;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void preUpdate() { updatedAt = OffsetDateTime.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getExternalSubId() { return externalSubId; }
    public void setExternalSubId(String externalSubId) { this.externalSubId = externalSubId; }
    public String getExternalCustomerId() { return externalCustomerId; }
    public void setExternalCustomerId(String externalCustomerId) { this.externalCustomerId = externalCustomerId; }
    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public OffsetDateTime getCurrentPeriodStart() { return currentPeriodStart; }
    public void setCurrentPeriodStart(OffsetDateTime currentPeriodStart) { this.currentPeriodStart = currentPeriodStart; }
    public OffsetDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
    public void setCurrentPeriodEnd(OffsetDateTime currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    public OffsetDateTime getTrialEndAt() { return trialEndAt; }
    public void setTrialEndAt(OffsetDateTime trialEndAt) { this.trialEndAt = trialEndAt; }
    public boolean isCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) { this.cancelAtPeriodEnd = cancelAtPeriodEnd; }
    public OffsetDateTime getCanceledAt() { return canceledAt; }
    public void setCanceledAt(OffsetDateTime canceledAt) { this.canceledAt = canceledAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public OffsetDateTime getPausedAt() { return pausedAt; }
    public void setPausedAt(OffsetDateTime pausedAt) { this.pausedAt = pausedAt; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getLastDeclineCode() { return lastDeclineCode; }
    public void setLastDeclineCode(String lastDeclineCode) { this.lastDeclineCode = lastDeclineCode; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
