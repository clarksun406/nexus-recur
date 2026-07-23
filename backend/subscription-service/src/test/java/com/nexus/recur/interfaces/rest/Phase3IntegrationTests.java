package com.nexus.recur.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.recur.application.service.SubscriptionService;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.repository.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase3IntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private com.nexus.recur.application.service.WalletService walletService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PaymentMethodRepository paymentMethodRepository;
    @Autowired private UsageRecordRepository usageRecordRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private FxTransactionRepository fxTransactionRepository;
    @Autowired private PaymentOrderRepository paymentOrderRepository;
    @Autowired private WebhookEndpointRepository webhookEndpointRepository;
    @Autowired private WebhookDeliveryRepository webhookDeliveryRepository;
    @Autowired private PortalTokenRepository portalTokenRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository walletTransactionRepository;
    @Autowired private SubscriptionPlanRepository planRepository;
    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private SubscriptionInvoiceRepository invoiceRepository;
    @Autowired private SubscriptionEventRepository eventRepository;
    @Autowired private MerchantRepository merchantRepository;

    @BeforeEach
    void cleanDatabase() {
        webhookDeliveryRepository.deleteAll();
        webhookEndpointRepository.deleteAll();
        portalTokenRepository.deleteAll();
        paymentOrderRepository.deleteAll();
        fxTransactionRepository.deleteAll();
        refundRepository.deleteAll();
        usageRecordRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        customerRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        eventRepository.deleteAll();
        invoiceRepository.deleteAll();
        subscriptionRepository.deleteAll();
        planRepository.deleteAll();
        merchantRepository.deleteAll();
    }

    @Test
    void customerCrudAndPaymentMethods() throws Exception {
        JsonNode customer = performJson(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"merchantId":"mch_1","email":"test@example.com","name":"Test User","phone":"+1234567890"}
                        """));
        String customerId = customer.path("data").path("id").asText();
        org.assertj.core.api.Assertions.assertThat(customerId).startsWith("cus_");

        mockMvc.perform(get("/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        mockMvc.perform(get("/v1/customers").param("merchantId", "mch_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        JsonNode pm = performJson(post("/v1/customers/{id}/payment-methods", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"type":"card","provider":"stripe","cardBrand":"visa","cardLast4":"4242","expMonth":12,"expYear":2028}
                        """));
        String pmId = pm.path("data").path("id").asText();
        org.assertj.core.api.Assertions.assertThat(pmId).startsWith("pm_");

        mockMvc.perform(get("/v1/customers/{id}/payment-methods", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].cardLast4").value("4242"));
    }

    @Test
    void usageReportIdempotency() throws Exception {
        String planId = createPlan("metered");
        String subId = createSubscription(planId, "usage_user");

        performJson(post("/v1/usage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"subscriptionId":"%s","planId":"%s","quantity":100,"unitName":"api_calls","idempotencyKey":"idem_001"}
                        """.formatted(subId, planId)));

        mockMvc.perform(post("/v1/usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriptionId":"%s","planId":"%s","quantity":100,"unitName":"api_calls","idempotencyKey":"idem_001"}
                                """.formatted(subId, planId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DUPLICATE_USAGE"));

        mockMvc.perform(get("/v1/usage").param("subscriptionId", subId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    void fxExchangeBetweenCurrencies() throws Exception {
        String planId = createPlan("default");
        String subId = createSubscription(planId, "fx_user");
        activateAndPay(subId, 100.0, "USD");

        JsonNode wallets = objectMapper.readTree(mockMvc.perform(get("/v1/wallets").param("merchantId", "merchant_default"))
                .andReturn().getResponse().getContentAsString());
        String usdWalletId = wallets.path("data").get(0).path("id").asText();

        walletService.applyToWallet("merchant_default", "EUR", java.math.BigDecimal.ZERO,
                com.nexus.recur.domain.model.WalletTransactionType.adjustment, "test EUR wallet", "test", "test");

        JsonNode wallets2 = objectMapper.readTree(mockMvc.perform(get("/v1/wallets").param("merchantId", "merchant_default"))
                .andReturn().getResponse().getContentAsString());
        String eurWalletId = null;
        for (JsonNode w : wallets2.path("data")) {
            if ("EUR".equals(w.path("currency").asText())) { eurWalletId = w.path("id").asText(); break; }
        }
        org.assertj.core.api.Assertions.assertThat(eurWalletId).isNotNull();

        JsonNode fx = performJson(post("/v1/fx/exchange").param("merchantId", "merchant_default")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sourceWalletId":"%s","targetWalletId":"%s","sourceAmountCents":5000}
                        """.formatted(usdWalletId, eurWalletId)));

        org.assertj.core.api.Assertions.assertThat(fx.path("data").path("id").asText()).startsWith("fx_");
        org.assertj.core.api.Assertions.assertThat(fx.path("data").path("exchangeRate").asDouble()).isGreaterThan(0);

        mockMvc.perform(get("/v1/fx").param("merchantId", "merchant_default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    void paymentOrderWithSanctionsScreening() throws Exception {
        String planId = createPlan("default");
        String subId = createSubscription(planId, "po_user");
        activateAndPay(subId, 200.0, "USD");

        JsonNode wallets = objectMapper.readTree(mockMvc.perform(get("/v1/wallets").param("merchantId", "merchant_default"))
                .andReturn().getResponse().getContentAsString());
        String usdWalletId = wallets.path("data").get(0).path("id").asText();

        JsonNode order = performJson(post("/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"merchantId":"merchant_default","walletId":"%s","currency":"USD","amountCents":10000,
                         "method":"ach","beneficiaryName":"Acme Corp","beneficiaryAccount":"US123456",
                         "beneficiaryBank":"Chase","beneficiaryCountry":"US","purpose":"vendor payment"}
                        """.formatted(usdWalletId)));

        org.assertj.core.api.Assertions.assertThat(order.path("data").path("id").asText()).startsWith("po_");
        org.assertj.core.api.Assertions.assertThat(order.path("data").path("sanctionsResult").asText()).isEqualTo("pass");

        mockMvc.perform(get("/v1/payment-orders").param("merchantId", "merchant_default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void paymentOrderBlockedBySanctions() throws Exception {
        String planId = createPlan("default");
        String subId = createSubscription(planId, "po_blocked_user");
        activateAndPay(subId, 200.0, "USD");

        JsonNode wallets = objectMapper.readTree(mockMvc.perform(get("/v1/wallets").param("merchantId", "merchant_default"))
                .andReturn().getResponse().getContentAsString());
        String usdWalletId = wallets.path("data").get(0).path("id").asText();

        JsonNode order = performJson(post("/v1/payment-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"merchantId":"merchant_default","walletId":"%s","currency":"USD","amountCents":10000,
                         "method":"wire","beneficiaryName":"Blocked Entity","beneficiaryAccount":"XX999",
                         "beneficiaryCountry":"KP","purpose":"test"}
                        """.formatted(usdWalletId)));

        org.assertj.core.api.Assertions.assertThat(order.path("data").path("status").asText()).isEqualTo("blocked");
        org.assertj.core.api.Assertions.assertThat(order.path("data").path("sanctionsResult").asText()).isEqualTo("blocked");
    }

    @Test
    void webhookEndpointCrudAndDeliveries() throws Exception {
        JsonNode ep = performJson(post("/v1/webhook-endpoints")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"url":"https://example.com/hook","merchantId":"mch_1","events":["charge.succeeded","charge.failed"]}
                        """));
        String epId = ep.path("data").path("id").asText();
        org.assertj.core.api.Assertions.assertThat(epId).startsWith("whe_");
        org.assertj.core.api.Assertions.assertThat(ep.path("data").path("secret").asText()).startsWith("whsec_");

        mockMvc.perform(get("/v1/webhook-endpoints").param("merchantId", "mch_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        mockMvc.perform(put("/v1/webhook-endpoints/{id}", epId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"disabled"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("disabled"));

        mockMvc.perform(get("/v1/webhook-endpoints/{id}/deliveries", epId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void portalMagicLinkAndAuth() throws Exception {
        JsonNode customer = performJson(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"merchantId":"mch_1","email":"portal@example.com","name":"Portal User"}
                        """));
        String customerId = customer.path("data").path("id").asText();

        JsonNode link = performJson(post("/v1/portal/link").param("customerId", customerId));
        String portalUrl = link.path("data").path("portalUrl").asText();
        org.assertj.core.api.Assertions.assertThat(portalUrl).contains("/portal/auth?token=");

        String token = portalUrl.substring(portalUrl.indexOf("token=") + 6);

        JsonNode session = performJson(post("/v1/portal/auth").param("token", token));
        org.assertj.core.api.Assertions.assertThat(session.path("data").path("sessionId").asText()).startsWith("ses_");
        org.assertj.core.api.Assertions.assertThat(session.path("data").path("customerId").asText()).isEqualTo(customerId);

        mockMvc.perform(post("/v1/portal/auth").param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_USED"));
    }

    @Test
    void reconciliationReportReturnsData() throws Exception {
        String planId = createPlan("default");
        String subId = createSubscription(planId, "recon_user");
        activateAndPay(subId, 49.90, "USD");

        mockMvc.perform(get("/v1/reconciliation/report")
                        .param("year", String.valueOf(java.time.OffsetDateTime.now().getYear()))
                        .param("month", String.valueOf(java.time.OffsetDateTime.now().getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalInvoices").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void rejectsUnsupportedCurrency() throws Exception {
        mockMvc.perform(post("/v1/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bad","billingCycle":"monthly","price":10,"currency":"XYZ","status":"active"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_CURRENCY"));
    }

    @Test
    void supportsExpandedCurrencies() throws Exception {
        JsonNode plan = performJson(post("/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"JPY Plan","billingCycle":"monthly","price":1000,"currency":"JPY","status":"active"}
                        """));
        org.assertj.core.api.Assertions.assertThat(plan.path("data").path("currency").asText()).isEqualTo("JPY");
    }

    // --- helpers ---

    private String createPlan(String suffix) throws Exception {
        JsonNode response = performJson(post("/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Plan %s","billingCycle":"monthly","price":29.90,"currency":"USD","status":"active",
                         "billingType":"flat_rate","taxMode":"none"}
                        """.formatted(suffix)));
        return response.path("data").path("id").asText();
    }

    private String createSubscription(String planId, String userId) throws Exception {
        JsonNode response = performJson(post("/v1/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"planId":"%s","userId":"%s","successUrl":"https://example.com/ok","cancelUrl":"https://example.com/no"}
                        """.formatted(planId, userId)));
        return response.path("data").path("subscriptionId").asText();
    }

    private void activateAndPay(String subscriptionId, double amount, String currency) {
        Subscription sub = subscriptionService.getEntity(subscriptionId);
        subscriptionService.activate(sub, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");
        subscriptionService.recordPaid(sub, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29),
                BigDecimal.valueOf(amount), currency, "card", "txn_test_" + subscriptionId, "{}");
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
        String content = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }
}
