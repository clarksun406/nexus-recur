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
@Table(name = "webhook_endpoints", indexes = {
        @Index(name = "idx_webhook_endpoint_merchant", columnList = "merchantId"),
        @Index(name = "idx_webhook_endpoint_status", columnList = "status")
})
public class WebhookEndpoint {
    @Id
    @Column(length = 32)
    private String id;
    @Column(length = 64)
    private String merchantId;
    @Column(nullable = false, length = 512)
    private String url;
    @Column(nullable = false, length = 128)
    private String secret;
    @Column(columnDefinition = "text")
    private String subscribedEvents;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WebhookEndpointStatus status = WebhookEndpointStatus.active;
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getSubscribedEvents() { return subscribedEvents; }
    public void setSubscribedEvents(String subscribedEvents) { this.subscribedEvents = subscribedEvents; }
    public WebhookEndpointStatus getStatus() { return status; }
    public void setStatus(WebhookEndpointStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isSubscribedTo(String eventType) {
        if (subscribedEvents == null || subscribedEvents.isBlank()) return true;
        for (String e : subscribedEvents.split(",")) {
            if (e.trim().equals(eventType)) return true;
        }
        return false;
    }
}
