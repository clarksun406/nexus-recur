package com.nexus.gateway.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/webhooks")
public class WebhookReceiverController {

    @PostMapping("/provider")
    public ResponseEntity<Map<String, String>> receiveProviderWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
