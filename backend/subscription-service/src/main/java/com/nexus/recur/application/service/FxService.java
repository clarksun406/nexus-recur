package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.application.service.ExchangeRateService.QuoteResult;
import com.nexus.recur.domain.model.*;
import com.nexus.recur.domain.repository.FxTransactionRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class FxService {

    private static final Logger log = LoggerFactory.getLogger(FxService.class);

    private final FxTransactionRepository fxTransactionRepository;
    private final WalletService walletService;
    private final ExchangeRateService exchangeRateService;
    private final IdGenerator idGenerator;

    public FxService(FxTransactionRepository fxTransactionRepository,
                     WalletService walletService,
                     ExchangeRateService exchangeRateService,
                     IdGenerator idGenerator) {
        this.fxTransactionRepository = fxTransactionRepository;
        this.walletService = walletService;
        this.exchangeRateService = exchangeRateService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public FxResponse exchange(String merchantId, ExchangeRequest request) {
        Wallet sourceWallet = walletService.getEntity(request.sourceWalletId());
        if (!sourceWallet.getMerchantId().equals(merchantId)) {
            throw new BusinessException("WALLET_NOT_OWNED", "Source wallet does not belong to this merchant");
        }
        if (sourceWallet.getBalanceCents() < request.sourceAmountCents()) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "Source wallet has insufficient balance");
        }

        Wallet targetWallet = walletService.getEntity(request.targetWalletId());
        if (!targetWallet.getMerchantId().equals(merchantId)) {
            throw new BusinessException("WALLET_NOT_OWNED", "Target wallet does not belong to this merchant");
        }
        if (sourceWallet.getCurrency().equals(targetWallet.getCurrency())) {
            throw new BusinessException("SAME_CURRENCY", "Source and target wallets have the same currency");
        }

        QuoteResult quote = exchangeRateService.getQuote(
                sourceWallet.getCurrency(), targetWallet.getCurrency(), request.sourceAmountCents());

        FxTransaction fx = new FxTransaction();
        fx.setId(idGenerator.next("fx"));
        fx.setMerchantId(merchantId);
        fx.setSourceWalletId(request.sourceWalletId());
        fx.setTargetWalletId(request.targetWalletId());
        fx.setSourceCurrency(sourceWallet.getCurrency());
        fx.setTargetCurrency(targetWallet.getCurrency());
        fx.setSourceAmountCents(request.sourceAmountCents());
        fx.setTargetAmountCents(quote.targetAmount().longValue());
        fx.setExchangeRate(quote.rate());
        fx.setSpreadBps(quote.spreadBps());

        try {
            walletService.applyToWallet(merchantId, sourceWallet.getCurrency(),
                    BigDecimal.valueOf(-request.sourceAmountCents(), 2),
                    WalletTransactionType.fx, "FX out: " + sourceWallet.getCurrency() + " → " + targetWallet.getCurrency(),
                    "fx", fx.getId());

            walletService.applyToWallet(merchantId, targetWallet.getCurrency(),
                    BigDecimal.valueOf(quote.targetAmount().longValue(), 2),
                    WalletTransactionType.fx, "FX in: " + sourceWallet.getCurrency() + " → " + targetWallet.getCurrency(),
                    "fx", fx.getId());

            fx.setStatus(FxStatus.completed);
            fx.setCompletedAt(OffsetDateTime.now());
            log.info("FX exchange completed: {} {} → {} {} (rate={})",
                    request.sourceAmountCents(), sourceWallet.getCurrency(),
                    quote.targetAmount().longValue(), targetWallet.getCurrency(), quote.rate());
        } catch (Exception e) {
            fx.setStatus(FxStatus.failed);
            fx.setFailureReason(e.getMessage());
            log.error("FX exchange failed: {}", e.getMessage());
        }

        fxTransactionRepository.save(fx);
        return toResponse(fx);
    }

    @Transactional(readOnly = true)
    public FxResponse get(String fxId) {
        FxTransaction fx = fxTransactionRepository.findById(fxId)
                .orElseThrow(() -> new BusinessException("FX_NOT_FOUND", "FX transaction not found: " + fxId));
        return toResponse(fx);
    }

    @Transactional(readOnly = true)
    public Page<FxResponse> list(String merchantId, int page, int limit) {
        return fxTransactionRepository.findByMerchantId(merchantId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)))
                .map(this::toResponse);
    }

    private FxResponse toResponse(FxTransaction fx) {
        return new FxResponse(fx.getId(), fx.getMerchantId(), fx.getSourceWalletId(), fx.getTargetWalletId(),
                fx.getSourceCurrency(), fx.getTargetCurrency(), fx.getSourceAmountCents(), fx.getTargetAmountCents(),
                fx.getExchangeRate(), fx.getSpreadBps(), fx.getStatus(), fx.getFailureReason(),
                fx.getCreatedAt(), fx.getCompletedAt());
    }

    public record ExchangeRequest(String sourceWalletId, String targetWalletId, long sourceAmountCents) {}

    public record FxResponse(String id, String merchantId, String sourceWalletId, String targetWalletId,
                             String sourceCurrency, String targetCurrency,
                             long sourceAmountCents, long targetAmountCents,
                             BigDecimal exchangeRate, int spreadBps, FxStatus status,
                             String failureReason, OffsetDateTime createdAt, OffsetDateTime completedAt) {}
}
