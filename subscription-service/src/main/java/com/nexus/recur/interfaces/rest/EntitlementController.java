package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.EntitlementService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.EntitlementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/entitlements")
public class EntitlementController {
    private final EntitlementService entitlementService;

    public EntitlementController(EntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<EntitlementResponse>> check(@RequestParam String userId) {
        EntitlementResponse response = entitlementService.check(userId);
        HttpStatus status = response.entitled() ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(ApiResponse.ok(response));
    }
}
