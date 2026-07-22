package com.nexus.recur.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.config.SubscriptionProperties;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.UpgradeSubscriptionRequest;
import com.nexus.recur.interfaces.rest.dto.WebhookDtos.SubscriptionWebhookPayload;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {
    private final SubscriptionProperties properties;
    private final ObjectMapper objectMapper;
    private final SubscriptionService subscriptionService;

    public WebhookService(SubscriptionProperties properties, ObjectMapper objectMapper, SubscriptionService subscriptionService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.subscriptionService = subscriptionService;
    }

    public void handle(String rawPayload, String signature) {
        verifySignature(rawPayload, signature);
        SubscriptionWebhookPayload payload;
        try {
            payload = objectMapper.readValue(rawPayload, SubscriptionWebhookPayload.class);
        } catch (Exception ex) {
            throw new BusinessException("INVALID_WEBHOOK_PAYLOAD", "webhook payload is not valid json");
        }
        if (payload.eventType() == null || payload.eventType().isBlank()) {
            throw new BusinessException("WEBHOOK_EVENT_REQUIRED", "eventType is required");
        }
        Subscription subscription = subscriptionService.findByLocalOrExternalId(payload.subscriptionId(), payload.externalSubId());
        switch (payload.eventType()) {
            case "subscription.active", "subscription.trialing" -> subscriptionService.activate(subscription, payload.periodStart(), payload.periodEnd(), rawPayload);
            case "subscription.paid" -> subscriptionService.recordPaid(subscription, payload.periodStart(), payload.periodEnd(), payload.amount(), payload.currency(), payload.paymentMethod(), payload.externalTransactionId(), rawPayload);
            case "subscription.past_due" -> subscriptionService.markStatus(subscription, SubscriptionStatus.past_due, rawPayload);
            case "subscription.canceled" -> subscriptionService.markStatus(subscription, SubscriptionStatus.canceled, rawPayload);
            case "subscription.scheduled_cancel" -> subscriptionService.markStatus(subscription, SubscriptionStatus.scheduled_cancel, rawPayload);
            case "subscription.expired" -> subscriptionService.markStatus(subscription, SubscriptionStatus.expired, rawPayload);
            case "subscription.paused" -> subscriptionService.markStatus(subscription, SubscriptionStatus.paused, rawPayload);
            case "subscription.update" -> {
                if (payload.planId() != null && !payload.planId().isBlank()) {
                    subscriptionService.upgrade(subscription.getId(), new UpgradeSubscriptionRequest(payload.planId(), null));
                }
            }
            default -> throw new BusinessException("UNSUPPORTED_WEBHOOK_EVENT", "unsupported webhook event: " + payload.eventType());
        }
    }

    private void verifySignature(String rawPayload, String signature) {
        String secret = properties.getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            return;
        }
        if (signature == null || signature.isBlank()) {
            throw new BusinessException("WEBHOOK_SIGNATURE_REQUIRED", "X-Webhook-Signature is required", HttpStatus.UNAUTHORIZED);
        }
        String expected = hmacSha256(rawPayload, secret);
        if (!java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException("WEBHOOK_SIGNATURE_INVALID", "webhook signature is invalid", HttpStatus.UNAUTHORIZED);
        }
    }

    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("cannot compute webhook signature", ex);
        }
    }
}
