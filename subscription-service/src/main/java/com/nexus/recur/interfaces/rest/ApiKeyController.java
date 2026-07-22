package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.ApiKeyService;
import com.nexus.recur.application.service.ApiKeyService.CreatedKey;
import com.nexus.recur.domain.model.ApiKey;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.ApiKeyDtos.ApiKeyResponse;
import com.nexus.recur.interfaces.rest.dto.ApiKeyDtos.CreateApiKeyRequest;
import com.nexus.recur.interfaces.rest.dto.ApiKeyDtos.CreateApiKeyResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api-keys")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @CheckPermission("api_key:create")
    public ApiResponse<CreateApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest request) {
        CreatedKey created = apiKeyService.create(request.userId(), request.merchantId(), request.scope());
        return ApiResponse.ok(new CreateApiKeyResponse(created.id(), created.key(), created.prefix(),
                request.scope(), request.userId(), request.merchantId()));
    }

    @GetMapping
    @CheckPermission("api_key:read")
    public ApiResponse<PageResult<ApiKeyResponse>> list(@RequestParam String userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int limit) {
        var result = apiKeyService.list(userId, page, limit);
        var items = result.getContent().stream().map(this::toResponse).toList();
        return ApiResponse.ok(new PageResult<>(items, page, limit, result.getTotalElements()));
    }

    @DeleteMapping("/{keyId}")
    @CheckPermission("api_key:revoke")
    public ApiResponse<Void> revoke(@PathVariable String keyId) {
        apiKeyService.revoke(keyId);
        return ApiResponse.ok(null);
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return new ApiKeyResponse(key.getId(), key.getKeyPrefix(), key.getScope(),
                key.getUserId(), key.getMerchantId(), key.getStatus().name(),
                key.getCreatedAt(), key.getLastUsedAt());
    }
}
