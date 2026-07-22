package com.nexus.gateway.service;

import com.nexus.gateway.config.GatewayProperties;
import com.nexus.gateway.domain.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class WebhookNotifier {

    private static final Logger log = LoggerFactory.getLogger(WebhookNotifier.class);

    private final RestClient restClient;
    private final String merchantWebhookUrl;

    public WebhookNotifier(GatewayProperties properties) {
        this.merchantWebhookUrl = properties.merchantWebhookUrl();
        this.restClient = RestClient.builder()
                .baseUrl(merchantWebhookUrl)
                .build();
    }

    public void notifySuccess(PaymentIntent intent) {
        send("payment.succeeded", intent);
    }

    public void notifyFailure(PaymentIntent intent) {
        send("payment.failed", intent);
    }

    private void send(String eventType, PaymentIntent intent) {
        try {
            Map<String, Object> payload = Map.of(
                    "eventType", eventType,
                    "paymentIntentId", intent.getId(),
                    "merchantId", intent.getMerchantId(),
                    "amountCents", intent.getAmountCents(),
                    "currency", intent.getCurrency(),
                    "metadata", intent.getMetadata() != null ? intent.getMetadata() : "",
                    "declineCode", intent.getDeclineCode() != null ? intent.getDeclineCode() : ""
            );
            restClient.post()
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to notify merchant webhook for intent {}: {}", intent.getId(), e.getMessage());
        }
    }
}
