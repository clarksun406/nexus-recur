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
@Table(name = "webhook_deliveries", indexes = {
        @Index(name = "idx_wh_delivery_endpoint", columnList = "endpointId"),
        @Index(name = "idx_wh_delivery_status", columnList = "status")
})
public class WebhookDelivery {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String endpointId;
    @Column(nullable = false, length = 64)
    private String eventType;
    @Column(nullable = false, columnDefinition = "text")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WebhookDeliveryStatus status = WebhookDeliveryStatus.pending;
    @Column(nullable = false)
    private int attempts;
    @Column(nullable = false)
    private int maxAttempts = 6;
    private OffsetDateTime nextRetryAt;
    private Integer responseCode;
    @Column(columnDefinition = "text")
    private String responseMessage;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEndpointId() { return endpointId; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public WebhookDeliveryStatus getStatus() { return status; }
    public void setStatus(WebhookDeliveryStatus status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public OffsetDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(OffsetDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
