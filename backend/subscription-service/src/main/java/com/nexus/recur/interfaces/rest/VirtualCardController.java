package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.VirtualCardService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.VirtualCardDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/virtual-cards")
public class VirtualCardController {

    private final VirtualCardService virtualCardService;

    public VirtualCardController(VirtualCardService virtualCardService) {
        this.virtualCardService = virtualCardService;
    }

    @PostMapping
    @CheckPermission("virtualcard:issue")
    public ApiResponse<VirtualCardResponse> issue(@RequestBody IssueCardRequest request, HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        return ApiResponse.ok(virtualCardService.issue(merchantId, request));
    }

    @GetMapping
    @CheckPermission("virtualcard:read")
    public ApiResponse<PageResult<VirtualCardResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {
        String merchantId = (String) httpRequest.getAttribute("merchantId");
        if (merchantId == null) merchantId = "merchant_default";
        Page<VirtualCardResponse> result = virtualCardService.list(merchantId, page, limit);
        return ApiResponse.ok(new PageResult<>(result.getContent(), page, limit, result.getTotalElements()));
    }

    @GetMapping("/{cardId}")
    @CheckPermission("virtualcard:read")
    public ApiResponse<VirtualCardResponse> get(@PathVariable String cardId) {
        return ApiResponse.ok(virtualCardService.get(cardId));
    }

    @PostMapping("/{cardId}/freeze")
    @CheckPermission("virtualcard:manage")
    public ApiResponse<VirtualCardResponse> freeze(@PathVariable String cardId) {
        return ApiResponse.ok(virtualCardService.freeze(cardId));
    }

    @PostMapping("/{cardId}/unfreeze")
    @CheckPermission("virtualcard:manage")
    public ApiResponse<VirtualCardResponse> unfreeze(@PathVariable String cardId) {
        return ApiResponse.ok(virtualCardService.unfreeze(cardId));
    }

    @PostMapping("/{cardId}/close")
    @CheckPermission("virtualcard:manage")
    public ApiResponse<VirtualCardResponse> close(@PathVariable String cardId) {
        return ApiResponse.ok(virtualCardService.close(cardId));
    }

    @PostMapping("/{cardId}/spend")
    @CheckPermission("virtualcard:manage")
    public ApiResponse<VirtualCardResponse> spend(@PathVariable String cardId, @RequestBody SpendRequest request) {
        return ApiResponse.ok(virtualCardService.recordSpend(cardId, request.amountCents()));
    }
}
