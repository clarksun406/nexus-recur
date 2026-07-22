package com.nexus.recur.interfaces.rest;

import com.nexus.recur.interfaces.rest.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@ConditionalOnProperty(prefix = "payment-gateway", name = "enabled", havingValue = "true")
public class PaymentProxyController {

    private final RestClient restClient;

    public PaymentProxyController(@Value("${payment-gateway.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @GetMapping("/intents/{id}")
    public ApiResponse<Map<?, ?>> getIntent(@PathVariable String id) {
        Map<?, ?> result = restClient.get()
                .uri("/v1/payments/intents/{id}", id)
                .retrieve()
                .body(Map.class);
        return ApiResponse.ok(result);
    }

    @GetMapping("/refunds/{id}")
    public ApiResponse<Map<?, ?>> getRefund(@PathVariable String id) {
        Map<?, ?> result = restClient.get()
                .uri("/v1/payments/refunds/{id}", id)
                .retrieve()
                .body(Map.class);
        return ApiResponse.ok(result);
    }
}
