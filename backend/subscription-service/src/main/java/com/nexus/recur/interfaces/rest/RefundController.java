package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.RefundService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.RefundDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    @CheckPermission("refund:create")
    public ApiResponse<RefundResponse> create(@Valid @RequestBody CreateRefundRequest request) {
        return ApiResponse.ok(refundService.create(request));
    }

    @PostMapping("/{refundId}/approve")
    @CheckPermission("refund:approve")
    public ApiResponse<RefundResponse> approve(@PathVariable String refundId,
                                               @Valid @RequestBody ApproveRefundRequest request) {
        return ApiResponse.ok(refundService.approve(refundId, request.approvedBy()));
    }

    @GetMapping("/{refundId}")
    @CheckPermission("refund:read")
    public ApiResponse<RefundResponse> get(@PathVariable String refundId) {
        return ApiResponse.ok(refundService.get(refundId));
    }

    @GetMapping
    @CheckPermission("refund:read")
    public ApiResponse<Page<RefundResponse>> list(
            @RequestParam String merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(refundService.list(merchantId, page, limit));
    }
}
