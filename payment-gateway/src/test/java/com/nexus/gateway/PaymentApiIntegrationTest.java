package com.nexus.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndConfirmIntent_succeeds() throws Exception {
        String createBody = """
                {"merchantId":"m1","amountCents":999,"currency":"USD","description":"test","metadata":"{}","paymentMethodId":"pm_1"}
                """;

        MvcResult createResult = mockMvc.perform(post("/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("requires_confirmation"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String intentId = created.get("id").asText();

        mockMvc.perform(post("/v1/payments/intents/" + intentId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("succeeded"))
                .andExpect(jsonPath("$.providerTransactionId").exists())
                .andExpect(jsonPath("$.attempts").isArray());
    }

    @Test
    void getIntent_afterCreate() throws Exception {
        String createBody = """
                {"merchantId":"m2","amountCents":500,"currency":"EUR","description":"get test","metadata":"{}","paymentMethodId":"pm_2"}
                """;

        MvcResult createResult = mockMvc.perform(post("/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String intentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/v1/payments/intents/" + intentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value("m2"))
                .andExpect(jsonPath("$.amountCents").value(500))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void cancelIntent_works() throws Exception {
        String createBody = """
                {"merchantId":"m3","amountCents":100,"currency":"USD","description":"cancel test","metadata":"{}","paymentMethodId":"pm_3"}
                """;

        MvcResult createResult = mockMvc.perform(post("/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String intentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/v1/payments/intents/" + intentId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("canceled"));
    }

    @Test
    void refund_afterSuccessfulPayment() throws Exception {
        String createBody = """
                {"merchantId":"m4","amountCents":2000,"currency":"USD","description":"refund test","metadata":"{}","paymentMethodId":"pm_4"}
                """;

        MvcResult createResult = mockMvc.perform(post("/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String intentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/v1/payments/intents/" + intentId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("succeeded"));

        String refundBody = """
                {"paymentIntentId":"%s","amountCents":2000,"currency":"USD","reason":"customer request"}
                """.formatted(intentId);

        mockMvc.perform(post("/v1/payments/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refundBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("succeeded"));
    }

    @Test
    void confirmAlreadyConfirmed_returns409() throws Exception {
        String createBody = """
                {"merchantId":"m5","amountCents":100,"currency":"USD","description":"double confirm","metadata":"{}","paymentMethodId":"pm_5"}
                """;

        MvcResult createResult = mockMvc.perform(post("/v1/payments/intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String intentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/v1/payments/intents/" + intentId + "/confirm"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/payments/intents/" + intentId + "/confirm"))
                .andExpect(status().isConflict());
    }

    @Test
    void getIntent_notFound_returns404() throws Exception {
        mockMvc.perform(get("/v1/payments/intents/pi_nonexistent"))
                .andExpect(status().isNotFound());
    }
}
