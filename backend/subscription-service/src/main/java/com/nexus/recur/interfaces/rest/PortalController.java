package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.PortalService;
import com.nexus.recur.application.service.PortalService.PortalSession;
import com.nexus.recur.application.service.PaymentMethodService;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionInvoice;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PaymentMethodDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/portal")
public class PortalController {

    private final PortalService portalService;
    private final PaymentMethodService paymentMethodService;

    public PortalController(PortalService portalService, PaymentMethodService paymentMethodService) {
        this.portalService = portalService;
        this.paymentMethodService = paymentMethodService;
    }

    @PostMapping("/link")
    @CheckPermission("customer_portal:generate_link")
    public ApiResponse<Map<String, String>> generateLink(@RequestParam String customerId) {
        String link = portalService.generateMagicLink(customerId);
        return ApiResponse.ok(Map.of("portalUrl", link));
    }

    @PostMapping("/auth")
    public ApiResponse<PortalSession> authenticate(@RequestParam String token) {
        return ApiResponse.ok(portalService.authenticate(token));
    }

    @GetMapping("/subscriptions")
    public ApiResponse<List<Subscription>> subscriptions(@RequestParam String customerId) {
        return ApiResponse.ok(portalService.getSubscriptions(customerId));
    }

    @GetMapping("/invoices")
    public ApiResponse<Page<SubscriptionInvoice>> invoices(@RequestParam String customerId,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(portalService.getInvoices(customerId, page, limit));
    }

    @GetMapping("/payment-methods")
    public ApiResponse<List<PaymentMethodResponse>> paymentMethods(@RequestParam String customerId) {
        return ApiResponse.ok(paymentMethodService.list(customerId));
    }

    @PostMapping("/payment-methods")
    public ApiResponse<PaymentMethodResponse> addPaymentMethod(@RequestParam String customerId,
                                                               @RequestBody CreatePaymentMethodRequest request) {
        return ApiResponse.ok(paymentMethodService.create(customerId, request));
    }

    @PostMapping("/subscriptions/{subscriptionId}/cancel")
    public ApiResponse<Void> cancel(@RequestParam String customerId,
                                    @PathVariable String subscriptionId,
                                    @RequestParam(required = false) String reason) {
        portalService.cancelSubscription(customerId, subscriptionId, reason);
        return ApiResponse.ok(null);
    }

    @PostMapping("/subscriptions/{subscriptionId}/resume")
    public ApiResponse<Void> resume(@RequestParam String customerId,
                                    @PathVariable String subscriptionId) {
        portalService.resumeSubscription(customerId, subscriptionId);
        return ApiResponse.ok(null);
    }
}
