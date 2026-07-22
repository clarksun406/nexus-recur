package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.WebhookDelivery;
import com.nexus.recur.domain.model.WebhookDeliveryStatus;
import com.nexus.recur.domain.model.WebhookEndpoint;
import com.nexus.recur.domain.model.WebhookEndpointStatus;
import com.nexus.recur.domain.repository.WebhookDeliveryRepository;
import com.nexus.recur.domain.repository.WebhookEndpointRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.infrastructure.webhook.WebhookHttpClient;
import com.nexus.recur.infrastructure.webhook.WebhookSigner;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryService.class);
    private static final Duration[] RETRY_DELAYS = {
            Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(30),
            Duration.ofHours(2), Duration.ofHours(12), Duration.ofHours(24)
    };

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookHttpClient httpClient;
    private final WebhookSigner signer;
    private final IdGenerator idGenerator;

    public WebhookDeliveryService(WebhookEndpointRepository endpointRepository,
                                  WebhookDeliveryRepository deliveryRepository,
                                  WebhookHttpClient httpClient,
                                  WebhookSigner signer,
                                  IdGenerator idGenerator) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.httpClient = httpClient;
        this.signer = signer;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public void publish(String eventType, String payload, String merchantId) {
        List<WebhookEndpoint> endpoints = endpointRepository.findByMerchantId(merchantId).stream()
                .filter(e -> e.getStatus() == WebhookEndpointStatus.active)
                .filter(e -> e.isSubscribedTo(eventType))
                .toList();

        for (WebhookEndpoint endpoint : endpoints) {
            WebhookDelivery delivery = new WebhookDelivery();
            delivery.setId(idGenerator.next("whd"));
            delivery.setEndpointId(endpoint.getId());
            delivery.setEventType(eventType);
            delivery.setPayload(payload);
            delivery.setStatus(WebhookDeliveryStatus.pending);
            delivery.setAttempts(0);
            delivery.setMaxAttempts(RETRY_DELAYS.length);
            delivery.setCreatedAt(OffsetDateTime.now());
            delivery = deliveryRepository.save(delivery);
            attemptDelivery(delivery, endpoint);
        }
    }

    @Transactional
    public void processRetries() {
        List<WebhookDelivery> pending = deliveryRepository
                .findByStatusAndNextRetryAtBefore(WebhookDeliveryStatus.pending, OffsetDateTime.now());
        for (WebhookDelivery delivery : pending) {
            WebhookEndpoint endpoint = endpointRepository.findById(delivery.getEndpointId()).orElse(null);
            if (endpoint == null || endpoint.getStatus() != WebhookEndpointStatus.active) {
                delivery.setStatus(WebhookDeliveryStatus.disabled);
                deliveryRepository.save(delivery);
                continue;
            }
            attemptDelivery(delivery, endpoint);
        }
    }

    private void attemptDelivery(WebhookDelivery delivery, WebhookEndpoint endpoint) {
        String signature = signer.sign(delivery.getPayload(), endpoint.getSecret());
        WebhookHttpClient.WebhookResponse response = httpClient.post(endpoint.getUrl(), delivery.getPayload(), signature);

        delivery.setResponseCode(response.statusCode());
        delivery.setResponseMessage(response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            delivery.setStatus(WebhookDeliveryStatus.delivered);
            delivery.setNextRetryAt(null);
        } else {
            int nextAttempt = delivery.getAttempts() + 1;
            delivery.setAttempts(nextAttempt);
            if (nextAttempt >= delivery.getMaxAttempts()) {
                delivery.setStatus(WebhookDeliveryStatus.failed);
                delivery.setNextRetryAt(null);
                endpoint.setStatus(WebhookEndpointStatus.disabled);
                endpointRepository.save(endpoint);
                log.warn("webhook endpoint {} disabled after {} failed attempts", endpoint.getId(), nextAttempt);
            } else {
                delivery.setStatus(WebhookDeliveryStatus.pending);
                delivery.setNextRetryAt(OffsetDateTime.now().plus(RETRY_DELAYS[nextAttempt - 1]));
            }
        }
        deliveryRepository.save(delivery);
    }
}
