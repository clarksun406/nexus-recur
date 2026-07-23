package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.LegalEntityService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.LegalEntityDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/legal-entities")
public class LegalEntityController {

    private final LegalEntityService legalEntityService;

    public LegalEntityController(LegalEntityService legalEntityService) {
        this.legalEntityService = legalEntityService;
    }

    @PostMapping
    @CheckPermission("entity:manage")
    public ApiResponse<LegalEntityResponse> create(@RequestBody CreateEntityRequest request) {
        return ApiResponse.ok(legalEntityService.create(request));
    }

    @GetMapping
    @CheckPermission("entity:read")
    public ApiResponse<List<LegalEntityResponse>> list(@RequestParam(required = false) String country) {
        if (country != null && !country.isBlank()) {
            return ApiResponse.ok(legalEntityService.listByCountry(country));
        }
        return ApiResponse.ok(legalEntityService.list());
    }

    @GetMapping("/{entityId}")
    @CheckPermission("entity:read")
    public ApiResponse<LegalEntityResponse> get(@PathVariable String entityId) {
        return ApiResponse.ok(legalEntityService.get(entityId));
    }

    @PutMapping("/{entityId}")
    @CheckPermission("entity:manage")
    public ApiResponse<LegalEntityResponse> update(@PathVariable String entityId, @RequestBody UpdateEntityRequest request) {
        return ApiResponse.ok(legalEntityService.update(entityId, request));
    }

    @PostMapping("/{entityId}/merchants")
    @CheckPermission("entity:manage")
    public ApiResponse<Void> assignMerchant(@PathVariable String entityId, @RequestBody AssignMerchantRequest request) {
        legalEntityService.assignMerchant(entityId, request.merchantId());
        return ApiResponse.ok(null);
    }
}
