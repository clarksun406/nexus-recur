package com.nexus.recur.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionProperties {
    private String webhookSecret = "change-me";
    private String checkoutBaseUrl = "https://checkout.example.local";
    private int webhookConnectTimeoutMs = 5000;
    private int webhookReadTimeoutMs = 10000;

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getCheckoutBaseUrl() { return checkoutBaseUrl; }
    public void setCheckoutBaseUrl(String checkoutBaseUrl) { this.checkoutBaseUrl = checkoutBaseUrl; }
    public int getWebhookConnectTimeoutMs() { return webhookConnectTimeoutMs; }
    public void setWebhookConnectTimeoutMs(int webhookConnectTimeoutMs) { this.webhookConnectTimeoutMs = webhookConnectTimeoutMs; }
    public int getWebhookReadTimeoutMs() { return webhookReadTimeoutMs; }
    public void setWebhookReadTimeoutMs(int webhookReadTimeoutMs) { this.webhookReadTimeoutMs = webhookReadTimeoutMs; }
}
