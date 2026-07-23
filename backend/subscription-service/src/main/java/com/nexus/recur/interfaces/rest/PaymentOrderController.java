package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.PaymentOrderService;
import com.nexus.recur.domain.model.PaymentOrder;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.PaymentOrderDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payment-orders")
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;

    public PaymentOrderController(PaymentOrderService paymentOrderService) {
        this.paymentOrderService = paymentOrderService;
    }

    @PostMapping
    @CheckPermission("wallet:payment:create")
    public ApiResponse<PaymentOrderResponse> create(@Valid @RequestBody CreatePaymentOrderRequest request) {
        PaymentOrder order = paymentOrderService.create(
                request.merchantId(), request.walletId(), request.currency(), request.amountCents(),
                request.method(), request.beneficiaryName(), request.beneficiaryAccount(),
                request.beneficiaryBank(), request.beneficiaryCountry(), request.purpose());
        return ApiResponse.ok(toResponse(order));
    }

    @GetMapping("/{id}")
    @CheckPermission("wallet:payment:read")
    public ApiResponse<PaymentOrderResponse> get(@PathVariable String id) {
        return ApiResponse.ok(toResponse(paymentOrderService.get(id)));
    }

    @GetMapping
    @CheckPermission("wallet:payment:read")
    public ApiResponse<PageResult<PaymentOrderResponse>> list(
            @RequestParam(required = false) String merchantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<PaymentOrder> result = paymentOrderService.list(merchantId, status, page, limit);
        List<PaymentOrderResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return ApiResponse.ok(new PageResult<>(items, page, limit, result.getTotalElements()));
    }

    @PostMapping("/{id}/approve")
    @CheckPermission("wallet:payment:approve")
    public ApiResponse<PaymentOrderResponse> approve(@PathVariable String id, @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.ok(toResponse(paymentOrderService.approve(id, request.approverId())));
    }

    @PostMapping("/{id}/reject")
    @CheckPermission("wallet:payment:approve")
    public ApiResponse<PaymentOrderResponse> reject(@PathVariable String id, @Valid @RequestBody ApprovalRequest request) {
        return ApiResponse.ok(toResponse(paymentOrderService.reject(id, request.approverId())));
    }

    @PostMapping("/{id}/complete")
    @CheckPermission("wallet:payment:create")
    public ApiResponse<PaymentOrderResponse> complete(@PathVariable String id) {
        return ApiResponse.ok(toResponse(paymentOrderService.complete(id)));
    }

    private PaymentOrderResponse toResponse(PaymentOrder o) {
        return new PaymentOrderResponse(o.getId(), o.getMerchantId(), o.getWalletId(), o.getCurrency(),
                o.getAmountCents(), o.getMethod().name(), o.getBeneficiaryName(), o.getBeneficiaryAccount(),
                o.getBeneficiaryBank(), o.getBeneficiaryCountry(), o.getPurpose(), o.getStatus().name(),
                o.getSanctionsResult() != null ? o.getSanctionsResult().name() : null,
                o.getApprovedBy(), o.getReferenceNumber(), o.getCreatedAt());
    }
}
