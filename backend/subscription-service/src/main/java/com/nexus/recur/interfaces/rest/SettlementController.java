package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.SettlementService;
import com.nexus.recur.domain.model.SettlementStatus;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.SettlementDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    @CheckPermission("settlement:initiate")
    public ApiResponse<SettlementResponse> initiate(@RequestBody InitiateSettlementRequest request, HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        return ApiResponse.ok(settlementService.initiate(merchantId, request));
    }

    @GetMapping
    @CheckPermission("settlement:read")
    public ApiResponse<PageResult<SettlementResponse>> list(
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        return ApiResponse.ok(settlementService.list(merchantId, status, page, limit));
    }

    @GetMapping("/{settlementId}")
    @CheckPermission("settlement:read")
    public ApiResponse<SettlementResponse> get(@PathVariable String settlementId) {
        return ApiResponse.ok(settlementService.get(settlementId));
    }

    @PostMapping("/{settlementId}/approve")
    @CheckPermission("settlement:approve")
    public ApiResponse<SettlementResponse> approve(@PathVariable String settlementId, @RequestBody ApproveSettlementRequest request) {
        return ApiResponse.ok(settlementService.approve(settlementId, request.approverId()));
    }

    @PostMapping("/{settlementId}/reject")
    @CheckPermission("settlement:approve")
    public ApiResponse<SettlementResponse> reject(@PathVariable String settlementId, @RequestBody RejectSettlementRequest request) {
        return ApiResponse.ok(settlementService.reject(settlementId, request.approverId(), request.reason()));
    }

    @PostMapping("/{settlementId}/complete")
    @CheckPermission("settlement:approve")
    public ApiResponse<SettlementResponse> complete(@PathVariable String settlementId) {
        return ApiResponse.ok(settlementService.complete(settlementId));
    }

    @PostMapping("/batch")
    @CheckPermission("settlement:initiate")
    public ApiResponse<java.util.List<SettlementResponse>> batchInitiate(
            @RequestBody java.util.List<InitiateSettlementRequest> requests,
            HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        return ApiResponse.ok(settlementService.batchInitiate(merchantId, requests));
    }
}
