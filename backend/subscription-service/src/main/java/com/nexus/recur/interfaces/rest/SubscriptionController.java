package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.SubscriptionService;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @CheckPermission("subscription:create")
    public ApiResponse<CreateSubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.ok(subscriptionService.create(request));
    }

    @GetMapping
    @CheckPermission("subscription:read")
    public ApiResponse<PageResult<SubscriptionResponse>> list(@RequestParam(required = false) String userId,
                                                              @RequestParam(required = false) SubscriptionStatus status,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(subscriptionService.list(userId, status, page, limit));
    }

    @GetMapping("/{subscriptionId}")
    @CheckPermission("subscription:read")
    public ApiResponse<SubscriptionResponse> get(@PathVariable String subscriptionId) {
        return ApiResponse.ok(subscriptionService.get(subscriptionId));
    }

    @PostMapping("/{subscriptionId}/cancel")
    @CheckPermission("subscription:cancel")
    public ApiResponse<CancelSubscriptionResponse> cancel(@PathVariable String subscriptionId, @RequestBody CancelSubscriptionRequest request) {
        return ApiResponse.ok(subscriptionService.cancel(subscriptionId, request));
    }

    @PostMapping("/{subscriptionId}/pause")
    @CheckPermission("subscription:pause")
    public ApiResponse<SubscriptionResponse> pause(@PathVariable String subscriptionId, @Valid @RequestBody PauseSubscriptionRequest request) {
        return ApiResponse.ok(subscriptionService.pause(subscriptionId, request));
    }

    @PostMapping("/{subscriptionId}/resume")
    @CheckPermission("subscription:resume")
    public ApiResponse<SubscriptionResponse> resume(@PathVariable String subscriptionId) {
        return ApiResponse.ok(subscriptionService.resume(subscriptionId));
    }

    @PostMapping("/{subscriptionId}/upgrade")
    @CheckPermission("subscription:upgrade")
    public ApiResponse<UpgradeSubscriptionResponse> upgrade(@PathVariable String subscriptionId, @Valid @RequestBody UpgradeSubscriptionRequest request) {
        return ApiResponse.ok(subscriptionService.upgrade(subscriptionId, request));
    }

    @GetMapping("/{subscriptionId}/invoices")
    @CheckPermission("charge:read")
    public ApiResponse<PageResult<InvoiceResponse>> invoices(@PathVariable String subscriptionId,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(subscriptionService.invoices(subscriptionId, page, limit));
    }

    @GetMapping("/{subscriptionId}/events")
    @CheckPermission("subscription:read")
    public ApiResponse<PageResult<EventResponse>> events(@PathVariable String subscriptionId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(subscriptionService.events(subscriptionId, page, limit));
    }
}
