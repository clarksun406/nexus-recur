package com.nexus.recur.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_events", indexes = {
        @Index(name = "idx_event_subscription", columnList = "subscriptionId"),
        @Index(name = "idx_event_created", columnList = "createdAt")
})
public class SubscriptionEvent {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String subscriptionId;
    @Column(nullable = false, length = 64)
    private String eventType;
    @Column(nullable = false, length = 32)
    private String source = "webhook";
    @Column(columnDefinition = "text")
    private String rawPayload;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
