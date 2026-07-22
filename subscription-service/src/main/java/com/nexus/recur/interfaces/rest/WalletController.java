package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.WalletService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.WalletDtos.RecordTransactionRequest;
import com.nexus.recur.interfaces.rest.dto.WalletDtos.TransactionResponse;
import com.nexus.recur.interfaces.rest.dto.WalletDtos.WalletResponse;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @CheckPermission("wallet:read")
    public ApiResponse<java.util.List<WalletResponse>> list(@RequestParam String merchantId) {
        return ApiResponse.ok(walletService.list(merchantId));
    }

    @GetMapping("/{walletId}")
    @CheckPermission("wallet:read")
    public ApiResponse<WalletResponse> get(@PathVariable String walletId) {
        return ApiResponse.ok(walletService.get(walletId));
    }

    @GetMapping("/{walletId}/transactions")
    @CheckPermission("wallet:read")
    public ApiResponse<PageResult<TransactionResponse>> transactions(@PathVariable String walletId,
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(walletService.transactions(walletId, page, limit));
    }

    @PostMapping("/{walletId}/transactions")
    @CheckPermission("wallet:adjust")
    public ApiResponse<TransactionResponse> recordTransaction(@PathVariable String walletId,
                                                              @Valid @RequestBody RecordTransactionRequest request) {
        return ApiResponse.ok(walletService.recordTransaction(walletId, request));
    }
}
