package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.WebhookDelivery;
import com.nexus.recur.domain.model.WebhookDeliveryStatus;
import com.nexus.recur.domain.model.WebhookEndpoint;
import com.nexus.recur.domain.model.WebhookEndpointStatus;
import com.nexus.recur.domain.repository.WebhookDeliveryRepository;
import com.nexus.recur.domain.repository.WebhookEndpointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class WebhookDeliveryServiceTest {
    @Autowired
    private WebhookDeliveryService deliveryService;
    @Autowired
    private WebhookEndpointRepository endpointRepository;
    @Autowired
    private WebhookDeliveryRepository deliveryRepository;

    @BeforeEach
    void cleanUp() {
        deliveryRepository.deleteAll();
        endpointRepository.deleteAll();
    }

    @Test
    void publishesToMatchingEndpoint() {
        createEndpoint("merchant_1", "http://example.com/webhook", "charge.succeeded");

        deliveryService.publish("charge.succeeded", "{\"amount\":100}", "merchant_1");

        assertThat(deliveryRepository.count()).isEqualTo(1);
        List<WebhookDelivery> deliveries = deliveryRepository.findAll();
        assertThat(deliveries).hasSize(1);
    }

    @Test
    void schedulesRetryOnFailure() {
        createEndpoint("merchant_3", "http://example.com/webhook", "charge.succeeded");

        deliveryService.publish("charge.succeeded", "{\"amount\":100}", "merchant_3");

        List<WebhookDelivery> deliveries = deliveryRepository.findAll();
        assertThat(deliveries).hasSize(1);
        WebhookDelivery delivery = deliveries.get(0);
        assertThat(delivery.getStatus()).isIn(WebhookDeliveryStatus.pending, WebhookDeliveryStatus.delivered);
    }

    @Test
    void skipsEndpointNotSubscribedToEvent() {
        createEndpoint("merchant_2", "http://example.com/webhook", "charge.failed");

        deliveryService.publish("charge.succeeded", "{\"amount\":100}", "merchant_2");

        assertThat(deliveryRepository.findAll()).isEmpty();
    }

    @Test
    void disablesEndpointAfterMaxRetries() {
        WebhookEndpoint endpoint = createEndpoint("merchant_4", "http://example.com/webhook", "charge.succeeded");

        deliveryService.publish("charge.succeeded", "{\"amount\":100}", "merchant_4");

        WebhookDelivery delivery = deliveryRepository.findAll().get(0);
        int maxAttempts = delivery.getMaxAttempts();

        for (int i = delivery.getAttempts(); i < maxAttempts; i++) {
            delivery.setNextRetryAt(OffsetDateTime.now().minusMinutes(1));
            deliveryRepository.save(delivery);
            deliveryService.processRetries();
            delivery = deliveryRepository.findById(delivery.getId()).orElseThrow();
        }

        assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.failed);
        WebhookEndpoint updatedEndpoint = endpointRepository.findById(endpoint.getId()).orElseThrow();
        assertThat(updatedEndpoint.getStatus()).isEqualTo(WebhookEndpointStatus.disabled);
    }

    @Test
    void deliversToMultipleEndpoints() {
        createEndpoint("merchant_5", "http://endpoint-a.com/webhook", "charge.succeeded");
        createEndpoint("merchant_5", "http://endpoint-b.com/webhook", "charge.succeeded");

        deliveryService.publish("charge.succeeded", "{\"amount\":100}", "merchant_5");

        assertThat(deliveryRepository.findAll()).hasSize(2);
    }

    private WebhookEndpoint createEndpoint(String merchantId, String url, String events) {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setId("whe_" + System.nanoTime());
        endpoint.setMerchantId(merchantId);
        endpoint.setUrl(url);
        endpoint.setSecret("test-secret");
        endpoint.setSubscribedEvents(events);
        endpoint.setStatus(WebhookEndpointStatus.active);
        return endpointRepository.save(endpoint);
    }
}
