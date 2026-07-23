package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.LicenseKeyService;
import com.nexus.recur.domain.model.LicenseKey;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.LicenseDtos.*;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/licenses")
public class LicenseController {

    private final LicenseKeyService licenseKeyService;

    public LicenseController(LicenseKeyService licenseKeyService) {
        this.licenseKeyService = licenseKeyService;
    }

    @PostMapping
    @CheckPermission("license:activate")
    public ApiResponse<LicenseResponse> generate(@Valid @RequestBody GenerateLicenseRequest request) {
        LicenseKey license = licenseKeyService.generate(request.merchantId(), request.planId(),
                request.subscriptionId(), request.maxActivations(), request.expiryDays());
        return ApiResponse.ok(toResponse(license));
    }

    @PostMapping("/validate")
    @CheckPermission("license:validate")
    public ApiResponse<LicenseResponse> validate(@Valid @RequestBody ValidateLicenseRequest request) {
        LicenseKey license = licenseKeyService.validate(request.licenseKey(), request.deviceFingerprint());
        return ApiResponse.ok(toResponse(license));
    }

    @GetMapping("/{id}")
    @CheckPermission("license:validate")
    public ApiResponse<LicenseResponse> get(@PathVariable String id) {
        return ApiResponse.ok(toResponse(licenseKeyService.get(id)));
    }

    @GetMapping
    @CheckPermission("license:validate")
    public ApiResponse<PageResult<LicenseResponse>> list(
            @RequestParam String merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<LicenseKey> result = licenseKeyService.list(merchantId, page, limit);
        List<LicenseResponse> items = result.getContent().stream().map(this::toResponse).toList();
        return ApiResponse.ok(new PageResult<>(items, page, limit, result.getTotalElements()));
    }

    @PostMapping("/{id}/suspend")
    @CheckPermission("license:revoke")
    public ApiResponse<LicenseResponse> suspend(@PathVariable String id) {
        return ApiResponse.ok(toResponse(licenseKeyService.suspend(id)));
    }

    @PostMapping("/{id}/revoke")
    @CheckPermission("license:revoke")
    public ApiResponse<LicenseResponse> revoke(@PathVariable String id) {
        return ApiResponse.ok(toResponse(licenseKeyService.revoke(id)));
    }

    @PostMapping("/{id}/reactivate")
    @CheckPermission("license:revoke")
    public ApiResponse<LicenseResponse> reactivate(@PathVariable String id) {
        return ApiResponse.ok(toResponse(licenseKeyService.reactivate(id)));
    }

    private LicenseResponse toResponse(LicenseKey l) {
        return new LicenseResponse(l.getId(), l.getLicenseKey(), l.getMerchantId(), l.getSubscriptionId(),
                l.getPlanId(), l.getStatus().name(), l.getDeviceFingerprint(), l.getMaxActivations(),
                l.getCurrentActivations(), l.getExpiresAt(), l.getLastValidatedAt(), l.getCreatedAt());
    }
}
