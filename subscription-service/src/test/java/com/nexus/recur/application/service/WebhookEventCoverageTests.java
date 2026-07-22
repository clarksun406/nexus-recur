package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionPlanRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.repository.WalletRepository;
import com.nexus.recur.domain.repository.WalletTransactionRepository;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanRequest;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.CreateSubscriptionRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "subscription.webhook-secret=test-secret")
class WebhookEventCoverageTests {
    @Autowired
    private PlanService planService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private WebhookService webhookService;
    @Autowired
    private SubscriptionPlanRepository planRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;
    @Autowired
    private SubscriptionEventRepository eventRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @BeforeEach
    void cleanDatabase() {
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        eventRepository.deleteAll();
        invoiceRepository.deleteAll();
        subscriptionRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    void dispatchesStatusChangingWebhookEvents() {
        assertWebhookStatus("subscription.past_due", SubscriptionStatus.past_due);
        assertWebhookStatus("subscription.canceled", SubscriptionStatus.canceled);
        assertWebhookStatus("subscription.scheduled_cancel", SubscriptionStatus.scheduled_cancel);
        assertWebhookStatus("subscription.expired", SubscriptionStatus.expired);
        assertWebhookStatus("subscription.paused", SubscriptionStatus.paused);
    }

    @Test
    void dispatchesUpdateWebhookToChangePlan() {
        String oldPlanId = createPlan("Basic", BigDecimal.TEN);
        String newPlanId = createPlan("Pro", BigDecimal.valueOf(30));
        String subscriptionId = createActiveSubscription(oldPlanId);
        String payload = """
                {
                  "eventType": "subscription.update",
                  "subscriptionId": "%s",
                  "planId": "%s"
                }
                """.formatted(subscriptionId, newPlanId);

        webhookService.handle(payload, hmac(payload));

        assertThat(subscriptionService.getEntity(subscriptionId).getPlanId()).isEqualTo(newPlanId);
        assertThat(eventRepository.findAll()).extracting("eventType").contains("plan_changed");
    }

    @Test
    void rejectsMalformedUnsupportedAndIncompleteWebhookPayloads() {
        assertThatThrownBy(() -> webhookService.handle("not-json", hmac("not-json")))
                .hasMessageContaining("not valid json");

        String unsupported = "{\"eventType\":\"subscription.unknown\",\"subscriptionId\":\"sub_missing\"}";
        assertThatThrownBy(() -> webhookService.handle(unsupported, hmac(unsupported)))
                .hasMessageContaining("subscription not found");

        String missingEventType = "{}";
        assertThatThrownBy(() -> webhookService.handle(missingEventType, hmac(missingEventType)))
                .hasMessageContaining("eventType is required");
    }

    private void assertWebhookStatus(String eventType, SubscriptionStatus expectedStatus) {
        String planId = createPlan(eventType.replace('.', '_'), BigDecimal.valueOf(29.90));
        String subscriptionId = createActiveSubscription(planId);
        String payload = """
                {
                  "eventType": "%s",
                  "subscriptionId": "%s"
                }
                """.formatted(eventType, subscriptionId);

        webhookService.handle(payload, hmac(payload));

        assertThat(subscriptionService.getEntity(subscriptionId).getStatus()).isEqualTo(expectedStatus);
    }

    private String createActiveSubscription(String planId) {
        String subscriptionId = subscriptionService.create(new CreateSubscriptionRequest(
                planId,
                "user_" + planId,
                "https://example.com/success",
                "https://example.com/cancel"
        )).subscriptionId();
        Subscription subscription = subscriptionService.getEntity(subscriptionId);
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");
        return subscriptionId;
    }

    private String createPlan(String name, BigDecimal price) {
        return planService.create(new PlanRequest(
                name,
                name + " plan",
                "prod_" + name,
                BillingCycle.monthly,
                price,
                "USD",
                0,
                Map.of("max_api_calls", 10000),
                PlanStatus.active,
                null, null, null, null, null, null
        )).id();
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec("test-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
