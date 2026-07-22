package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.WebhookService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/webhooks/subscription")
public class WebhookController {
    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ApiResponse<Void> handle(@RequestBody String rawPayload,
                                    @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {
        webhookService.handle(rawPayload, signature);
        return ApiResponse.ok(null);
    }
}
