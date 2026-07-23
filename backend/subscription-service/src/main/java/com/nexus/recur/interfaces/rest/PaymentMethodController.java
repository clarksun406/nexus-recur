package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.PaymentMethodService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PaymentMethodDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/customers/{customerId}/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    @CheckPermission("customer:read")
    public ApiResponse<List<PaymentMethodResponse>> list(@PathVariable String customerId) {
        return ApiResponse.ok(paymentMethodService.list(customerId));
    }

    @PostMapping
    @CheckPermission("customer:update")
    public ApiResponse<PaymentMethodResponse> create(@PathVariable String customerId,
                                                     @Valid @RequestBody CreatePaymentMethodRequest request) {
        return ApiResponse.ok(paymentMethodService.create(customerId, request));
    }

    @PostMapping("/{pmId}/set-default")
    @CheckPermission("customer:update")
    public ApiResponse<PaymentMethodResponse> setDefault(@PathVariable String customerId, @PathVariable String pmId) {
        return ApiResponse.ok(paymentMethodService.setDefault(pmId));
    }

    @PostMapping("/{pmId}/revoke")
    @CheckPermission("customer:update")
    public ApiResponse<PaymentMethodResponse> revoke(@PathVariable String customerId, @PathVariable String pmId) {
        return ApiResponse.ok(paymentMethodService.revoke(pmId));
    }
}
