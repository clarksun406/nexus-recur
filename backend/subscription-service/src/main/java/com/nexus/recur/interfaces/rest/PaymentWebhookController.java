package com.nexus.recur.interfaces.rest;

import com.nexus.recur.interfaces.rest.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/webhooks/payment")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    @PostMapping
    public ApiResponse<Void> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        String eventType = (String) payload.get("eventType");
        String paymentIntentId = (String) payload.get("paymentIntentId");
        log.info("Received payment webhook: type={}, intentId={}", eventType, paymentIntentId);
        return ApiResponse.ok(null);
    }
}
