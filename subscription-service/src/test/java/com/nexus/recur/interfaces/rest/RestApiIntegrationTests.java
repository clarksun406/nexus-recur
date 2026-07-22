package com.nexus.recur.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.recur.application.service.SubscriptionService;
import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionPlanRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.repository.WalletRepository;
import com.nexus.recur.domain.repository.WalletTransactionRepository;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestApiIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SubscriptionService subscriptionService;
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
    void managesPlanAndSubscriptionThroughRestApi() throws Exception {
        String planId = createPlan();

        mockMvc.perform(get("/v1/plans/{planId}", planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(planId))
                .andExpect(jsonPath("$.data.billingCycle").value("monthly"));

        mockMvc.perform(get("/v1/plans")
                        .param("status", "active")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(put("/v1/plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PlanRequest(
                                "Pro Plus",
                                "updated",
                                "prod_plus",
                                BillingCycle.annual,
                                BigDecimal.valueOf(299),
                                "usd",
                                0,
                                Map.of("storage_gb", 100),
                                PlanStatus.active,
                                null, null, null, null, null, null
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Pro Plus"))
                .andExpect(jsonPath("$.data.currency").value("USD"));

        JsonNode createSubscription = performJson(post("/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "planId": "%s",
                          "userId": "rest_user",
                          "successUrl": "https://example.com/success",
                          "cancelUrl": "https://example.com/cancel"
                        }
                        """.formatted(planId)));
        String subscriptionId = createSubscription.path("data").path("subscriptionId").asText();

        Subscription subscription = subscriptionService.getEntity(subscriptionId);
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");

        mockMvc.perform(get("/v1/subscriptions")
                        .param("userId", "rest_user")
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mockMvc.perform(get("/v1/subscriptions/{subscriptionId}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));

        mockMvc.perform(post("/v1/subscriptions/{subscriptionId}/pause", subscriptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"rest pause\",\"maxPauseDays\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("paused"));

        mockMvc.perform(post("/v1/subscriptions/{subscriptionId}/resume", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));

        mockMvc.perform(get("/v1/entitlements/check").param("userId", "rest_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.entitled").value(true));

        mockMvc.perform(post("/v1/subscriptions/{subscriptionId}/cancel", subscriptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"immediate\":false,\"reason\":\"rest cancel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("scheduled_cancel"));

        mockMvc.perform(post("/v1/plans/{planId}/archive", planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("archived"));
    }

    @Test
    void mapsValidationBusinessAndPaymentRequiredResponses() throws Exception {
        mockMvc.perform(post("/v1/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "billingCycle": "monthly",
                                  "price": 1,
                                  "currency": "USD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/v1/plans/{planId}", "missing_plan"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLAN_NOT_FOUND"));

        mockMvc.perform(get("/v1/entitlements/check").param("userId", "no_subscription_user"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.data.entitled").value(false));
    }

    @Test
    void mapsWebhookSignatureAndPayloadErrors() throws Exception {
        mockMvc.perform(post("/v1/webhooks/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"subscription.paid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("WEBHOOK_SIGNATURE_REQUIRED"));

        mockMvc.perform(post("/v1/webhooks/subscription")
                        .header("X-Webhook-Signature", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"subscription.paid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("signature is invalid")));
    }

    @Test
    void returnsDashboardStatsWithMetrics() throws Exception {
        String planId = createPlan();
        JsonNode createSub = performJson(post("/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "planId": "%s",
                          "userId": "dash_user",
                          "successUrl": "https://example.com/success",
                          "cancelUrl": "https://example.com/cancel"
                        }
                        """.formatted(planId)));
        String subscriptionId = createSub.path("data").path("subscriptionId").asText();

        Subscription subscription = subscriptionService.getEntity(subscriptionId);
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");
        subscriptionService.recordPaid(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29),
                BigDecimal.valueOf(29.90), "USD", "card", "txn_dash", "{}");

        mockMvc.perform(get("/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeSubscriptions").value(1))
                .andExpect(jsonPath("$.data.totalSubscriptions").value(1))
                .andExpect(jsonPath("$.data.totalCharges").value(1))
                .andExpect(jsonPath("$.data.chargeSuccessRate").value(100.0))
                .andExpect(jsonPath("$.data.revenueByCurrency.USD").value(29.90));
    }

    @Test
    void returnsSubscriptionEventsAndInvoices() throws Exception {
        String planId = createPlan();
        JsonNode createSub = performJson(post("/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "planId": "%s",
                          "userId": "evt_user",
                          "successUrl": "https://example.com/success",
                          "cancelUrl": "https://example.com/cancel"
                        }
                        """.formatted(planId)));
        String subscriptionId = createSub.path("data").path("subscriptionId").asText();

        Subscription subscription = subscriptionService.getEntity(subscriptionId);
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");
        subscriptionService.recordPaid(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29),
                BigDecimal.valueOf(29.90), "USD", "card", "txn_evt", "{}");

        mockMvc.perform(get("/v1/subscriptions/{subscriptionId}/events", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.items[0].eventType").exists());

        mockMvc.perform(get("/v1/subscriptions/{subscriptionId}/invoices", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].amount").value(29.90));
    }

    @Test
    void managesWalletsAndTransactions() throws Exception {
        String planId = createPlan();
        JsonNode createSub = performJson(post("/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "planId": "%s",
                          "userId": "wallet_user",
                          "successUrl": "https://example.com/success",
                          "cancelUrl": "https://example.com/cancel"
                        }
                        """.formatted(planId)));
        String subscriptionId = createSub.path("data").path("subscriptionId").asText();

        Subscription subscription = subscriptionService.getEntity(subscriptionId);
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");
        subscriptionService.recordPaid(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29),
                BigDecimal.valueOf(29.90), "USD", "card", "txn_wal", "{}");

        mockMvc.perform(get("/v1/wallets").param("merchantId", "merchant_default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].currency").value("USD"))
                .andExpect(jsonPath("$.data[0].balance").value(29.90));

        JsonNode wallets = objectMapper.readTree(mockMvc.perform(get("/v1/wallets").param("merchantId", "merchant_default"))
                .andReturn().getResponse().getContentAsString());
        String walletId = wallets.path("data").get(0).path("id").asText();

        mockMvc.perform(get("/v1/wallets/{walletId}/transactions", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].type").value("income"))
                .andExpect(jsonPath("$.data.items[0].amount").value(29.90));
    }

    private String createPlan() throws Exception {
        JsonNode response = performJson(post("/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Pro",
                          "description": "Pro plan",
                          "productId": "prod_pro",
                          "billingCycle": "monthly",
                          "price": 29.90,
                          "currency": "USD",
                          "trialDays": 0,
                          "features": {"max_api_calls": 10000},
                          "status": "active"
                        }
                        """));
        String planId = response.path("data").path("id").asText();
        org.assertj.core.api.Assertions.assertThat(planId).startsWith("plan_");
        return planId;
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
        String content = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }
}
