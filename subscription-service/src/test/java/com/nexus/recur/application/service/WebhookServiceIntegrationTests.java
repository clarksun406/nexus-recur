package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.PlanStatus;
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
class WebhookServiceIntegrationTests {
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
    void verifiesSignatureAndDispatchesPaidWebhook() {
        String planId = planService.create(new PlanRequest(
                "Pro",
                "Pro plan",
                "prod_pro",
                BillingCycle.monthly,
                BigDecimal.valueOf(29.90),
                "USD",
                0,
                Map.of("max_api_calls", 10000),
                PlanStatus.active,
                null, null, null, null, null, null
        )).id();
        String subscriptionId = subscriptionService.create(new CreateSubscriptionRequest(
                planId,
                "user_webhook",
                "https://example.com/success",
                "https://example.com/cancel"
        )).subscriptionId();

        String payload = """
                {
                  "eventType": "subscription.paid",
                  "subscriptionId": "%s",
                  "periodStart": "2026-07-13T00:00:00Z",
                  "periodEnd": "2026-08-13T00:00:00Z",
                  "amount": 29.90,
                  "currency": "USD",
                  "paymentMethod": "card",
                  "externalTransactionId": "txn_webhook"
                }
                """.formatted(subscriptionId);

        webhookService.handle(payload, hmac(payload, "test-secret"));

        assertThat(subscriptionService.getEntity(subscriptionId).getStatus()).isEqualTo(SubscriptionStatus.active);
        assertThat(invoiceRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll()).extracting("eventType").contains("renewed");
    }

    @Test
    void rejectsInvalidWebhookSignature() {
        assertThatThrownBy(() -> webhookService.handle("{\"eventType\":\"subscription.paid\"}", "bad-signature"))
                .hasMessageContaining("webhook signature is invalid");
    }

    private String hmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
