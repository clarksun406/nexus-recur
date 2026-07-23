package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.FxService;
import com.nexus.recur.application.service.FxService.ExchangeRequest;
import com.nexus.recur.application.service.FxService.FxResponse;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexusflow.permission.client.CheckPermission;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fx")
public class FxController {

    private final FxService fxService;

    public FxController(FxService fxService) {
        this.fxService = fxService;
    }

    @PostMapping("/exchange")
    @CheckPermission("wallet:fx:create")
    public ApiResponse<FxResponse> exchange(@RequestParam String merchantId,
                                            @RequestBody ExchangeRequest request) {
        return ApiResponse.ok(fxService.exchange(merchantId, request));
    }

    @GetMapping("/{fxId}")
    @CheckPermission("wallet:read")
    public ApiResponse<FxResponse> get(@PathVariable String fxId) {
        return ApiResponse.ok(fxService.get(fxId));
    }

    @GetMapping
    @CheckPermission("wallet:read")
    public ApiResponse<Page<FxResponse>> list(@RequestParam String merchantId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(fxService.list(merchantId, page, limit));
    }
}
