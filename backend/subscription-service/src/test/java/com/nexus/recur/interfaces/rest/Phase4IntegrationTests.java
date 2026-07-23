package com.nexus.recur.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.recur.application.service.MerchantService;
import com.nexus.recur.application.service.PlanService;
import com.nexus.recur.application.service.SubscriptionService;
import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.Merchant;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanRequest;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.CreateSubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Phase4IntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MerchantService merchantService;
    @Autowired private PlanService planService;
    @Autowired private SubscriptionService subscriptionService;

    @Test
    void routingRuleCrudAndResolve() throws Exception {
        String merchantId = ensureMerchant();

        mockMvc.perform(post("/v1/routing-rules")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerName":"stripe","priority":1,"currency":"USD","strategy":"priority","costPercentage":2.9,"successRate":0.96}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.providerName").value("stripe"));

        mockMvc.perform(post("/v1/routing-rules")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerName":"creem","priority":2,"currency":"USD","strategy":"priority","costPercentage":2.5,"successRate":0.93}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/routing-rules/resolve")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currency":"USD","amountCents":5000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("stripe"));
    }

    @Test
    void routingCostOptimized() throws Exception {
        String merchantId = ensureMerchant();

        mockMvc.perform(post("/v1/routing-rules")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerName":"adyen","priority":1,"currency":"EUR","strategy":"cost_optimized","costPercentage":1.9}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/routing-rules")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"providerName":"stripe","priority":2,"currency":"EUR","strategy":"cost_optimized","costPercentage":2.9}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/routing-rules/resolve")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currency":"EUR","amountCents":10000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("adyen"));
    }

    @Test
    void virtualCardLifecycle() throws Exception {
        String merchantId = ensureMerchant();

        MvcResult result = mockMvc.perform(post("/v1/virtual-cards")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"cus_test1","currency":"USD","spendingLimitCents":50000,"label":"Test Card"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.last4").exists())
                .andReturn();

        String cardId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        mockMvc.perform(post("/v1/virtual-cards/" + cardId + "/spend")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amountCents":20000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.spentCents").value(20000));

        mockMvc.perform(post("/v1/virtual-cards/" + cardId + "/freeze")
                        .header("X-Merchant-Id", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("frozen"));

        mockMvc.perform(post("/v1/virtual-cards/" + cardId + "/unfreeze")
                        .header("X-Merchant-Id", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("active"));

        mockMvc.perform(post("/v1/virtual-cards/" + cardId + "/close")
                        .header("X-Merchant-Id", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("closed"));
    }

    @Test
    void virtualCardSpendingLimitEnforced() throws Exception {
        String merchantId = ensureMerchant();

        MvcResult result = mockMvc.perform(post("/v1/virtual-cards")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"cus_test2","spendingLimitCents":10000}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String cardId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        mockMvc.perform(post("/v1/virtual-cards/" + cardId + "/spend")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amountCents":15000}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void legalEntityCrudAndAssign() throws Exception {
        String merchantId = ensureMerchant();

        MvcResult result = mockMvc.perform(post("/v1/legal-entities")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nexus US Inc","country":"US","taxId":"12-3456789","baseCurrency":"USD","primaryContact":"John"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nexus US Inc"))
                .andExpect(jsonPath("$.data.status").value("active"))
                .andReturn();

        String entityId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();

        mockMvc.perform(post("/v1/legal-entities/" + entityId + "/merchants")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("merchantId", merchantId))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/legal-entities?country=US")
                        .header("X-Merchant-Id", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Nexus US Inc"));
    }

    @Test
    void licenseKeyGenerateAndValidate() throws Exception {
        String merchantId = ensureMerchant();
        String planId = createPlanWithLicense();
        String subId = createSubscription(planId);

        MvcResult result = mockMvc.perform(post("/v1/licenses")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "merchantId", merchantId,
                                "planId", planId,
                                "subscriptionId", subId,
                                "maxActivations", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.licenseKey").exists())
                .andExpect(jsonPath("$.data.status").value("active"))
                .andReturn();

        String licenseKey = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("licenseKey").asText();

        mockMvc.perform(post("/v1/licenses/validate")
                        .header("X-Merchant-Id", merchantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "licenseKey", licenseKey,
                                "deviceFingerprint", "device-001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentActivations").value(1));
    }

    @Test
    void batchSettlementAndQuota() throws Exception {
        String merchantId = ensureMerchant();

        mockMvc.perform(get("/v1/settlements/quota")
                        .header("X-Merchant-Id", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.annualLimitCents").value(500000000))
                .andExpect(jsonPath("$.data.usedCents").value(0));
    }

    @Test
    void complianceExportReturnsCsv() throws Exception {
        String merchantId = ensureMerchant();

        mockMvc.perform(get("/v1/settlements/compliance-export")
                        .param("year", "2026")
                        .param("quarter", "1")
                        .header("X-Merchant-Id", merchantId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"));
    }

    private String ensureMerchant() {
        Merchant merchant = merchantService.register("Phase4 Test Merchant", "phase4@test.com");
        return merchant.getId();
    }

    private String createPlanWithLicense() {
        PlanRequest request = new PlanRequest(
                "License Plan", "Plan with license", "prod_lic", BillingCycle.monthly,
                new BigDecimal("49.99"), "USD", 0, null, null, null, null, null, null, true, null
        );
        return planService.create(request).id();
    }

    private String createSubscription(String planId) {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest(
                planId, "user_phase4", "http://localhost/success", "http://localhost/cancel"
        );
        return subscriptionService.create(request).subscriptionId();
    }
}
