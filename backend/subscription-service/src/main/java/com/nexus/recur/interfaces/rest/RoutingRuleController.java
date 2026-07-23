package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.PaymentRoutingService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.RoutingDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/routing-rules")
public class RoutingRuleController {

    private final PaymentRoutingService routingService;

    public RoutingRuleController(PaymentRoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping
    @CheckPermission("routing:manage")
    public ApiResponse<RoutingRuleResponse> create(@RequestBody CreateRoutingRuleRequest request, HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        return ApiResponse.ok(routingService.create(merchantId, request));
    }

    @GetMapping
    @CheckPermission("routing:read")
    public ApiResponse<List<RoutingRuleResponse>> list(HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        return ApiResponse.ok(routingService.list(merchantId));
    }

    @PutMapping("/{ruleId}")
    @CheckPermission("routing:manage")
    public ApiResponse<RoutingRuleResponse> update(@PathVariable String ruleId, @RequestBody UpdateRoutingRuleRequest request) {
        return ApiResponse.ok(routingService.update(ruleId, request));
    }

    @DeleteMapping("/{ruleId}")
    @CheckPermission("routing:manage")
    public ApiResponse<Void> delete(@PathVariable String ruleId) {
        routingService.delete(ruleId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/resolve")
    @CheckPermission("routing:read")
    public ApiResponse<RoutingDecision> resolve(@RequestBody ResolveRequest request, HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        long amount = request.amountCents() != null ? request.amountCents() : 0;
        String region = request.region() != null ? request.region() : "";
        return ApiResponse.ok(routingService.resolve(merchantId, request.currency(), amount, region));
    }
}
