package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.Wallet;
import com.nexus.recur.domain.model.WalletStatus;
import com.nexus.recur.domain.model.WalletTransaction;
import com.nexus.recur.domain.model.WalletTransactionType;
import com.nexus.recur.domain.repository.WalletRepository;
import com.nexus.recur.domain.repository.WalletTransactionRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.WalletDtos.RecordTransactionRequest;
import com.nexus.recur.interfaces.rest.dto.WalletDtos.TransactionResponse;
import com.nexus.recur.interfaces.rest.dto.WalletDtos.WalletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final IdGenerator idGenerator;

    public WalletService(WalletRepository walletRepository, WalletTransactionRepository transactionRepository, IdGenerator idGenerator) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> list(String merchantId) {
        return walletRepository.findByMerchantId(merchantId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WalletResponse get(String walletId) {
        return toResponse(getEntity(walletId));
    }

    @Transactional(readOnly = true)
    public PageResult<TransactionResponse> transactions(String walletId, int page, int limit) {
        getEntity(walletId);
        Page<WalletTransaction> result = transactionRepository.findByWalletId(walletId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)));
        return new PageResult<>(result.map(this::toTransactionResponse).getContent(), page, limit, result.getTotalElements());
    }

    @Transactional
    public TransactionResponse recordTransaction(String walletId, RecordTransactionRequest request) {
        Wallet wallet = getEntity(walletId);
        if (wallet.getStatus() == WalletStatus.frozen || wallet.getStatus() == WalletStatus.closed) {
            throw new BusinessException("WALLET_NOT_ACTIVE", "wallet is " + wallet.getStatus(), HttpStatus.BAD_REQUEST);
        }
        int amountCents = toCents(request.amount());
        int delta = switch (request.type()) {
            case income, adjustment -> amountCents;
            case expense, fx, settlement -> -amountCents;
        };
        wallet.setBalanceCents(wallet.getBalanceCents() + delta);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setId(idGenerator.next("wtx"));
        txn.setWalletId(walletId);
        txn.setType(request.type());
        txn.setAmountCents(amountCents);
        txn.setCurrency(request.currency().toUpperCase());
        txn.setDescription(request.description());
        txn.setReferenceType(request.referenceType());
        txn.setReferenceId(request.referenceId());
        transactionRepository.save(txn);
        return toTransactionResponse(txn);
    }

    @Transactional
    public void applyToWallet(String merchantId, String currency, BigDecimal amount, WalletTransactionType type,
                               String description, String referenceType, String referenceId) {
        String normalizedCurrency = currency == null || currency.isBlank() ? "USD" : currency.toUpperCase();
        com.nexus.recur.domain.model.SupportedCurrencies.validate(normalizedCurrency);
        Wallet wallet = walletRepository.findByMerchantIdAndCurrency(merchantId, normalizedCurrency)
                .orElseGet(() -> createWallet(merchantId, normalizedCurrency));
        int amountCents = toCents(amount);
        int delta = switch (type) {
            case income, adjustment -> amountCents;
            case expense, fx, settlement -> -amountCents;
        };
        wallet.setBalanceCents(wallet.getBalanceCents() + delta);
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setId(idGenerator.next("wtx"));
        txn.setWalletId(wallet.getId());
        txn.setType(type);
        txn.setAmountCents(amountCents);
        txn.setCurrency(normalizedCurrency);
        txn.setDescription(description);
        txn.setReferenceType(referenceType);
        txn.setReferenceId(referenceId);
        transactionRepository.save(txn);
    }

    @Transactional
    public void freeze(String walletId, long amountCents) {
        Wallet wallet = getEntity(walletId);
        if (wallet.getBalanceCents() < amountCents) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "cannot freeze: insufficient balance", HttpStatus.BAD_REQUEST);
        }
        wallet.setBalanceCents(wallet.getBalanceCents() - (int) amountCents);
        wallet.setPendingBalanceCents(wallet.getPendingBalanceCents() + (int) amountCents);
        walletRepository.save(wallet);
    }

    @Transactional
    public void unfreeze(String walletId, long amountCents) {
        Wallet wallet = getEntity(walletId);
        wallet.setPendingBalanceCents(wallet.getPendingBalanceCents() - (int) amountCents);
        wallet.setBalanceCents(wallet.getBalanceCents() + (int) amountCents);
        walletRepository.save(wallet);
    }

    @Transactional
    public void settlePending(String walletId, long amountCents) {
        Wallet wallet = getEntity(walletId);
        wallet.setPendingBalanceCents(wallet.getPendingBalanceCents() - (int) amountCents);
        walletRepository.save(wallet);
    }

    private Wallet createWallet(String merchantId, String currency) {
        Wallet wallet = new Wallet();
        wallet.setId(idGenerator.next("wal"));
        wallet.setMerchantId(merchantId);
        wallet.setCurrency(currency);
        wallet.setBalanceCents(0);
        wallet.setPendingBalanceCents(0);
        wallet.setStatus(WalletStatus.active);
        return walletRepository.save(wallet);
    }

    public Wallet getEntity(String walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new BusinessException("WALLET_NOT_FOUND", "wallet not found: " + walletId, HttpStatus.NOT_FOUND));
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getMerchantId(), wallet.getCurrency(),
                BigDecimal.valueOf(wallet.getBalanceCents(), 2), BigDecimal.valueOf(wallet.getPendingBalanceCents(), 2),
                wallet.getStatus(), wallet.getCreatedAt(), wallet.getUpdatedAt());
    }

    private TransactionResponse toTransactionResponse(WalletTransaction txn) {
        return new TransactionResponse(txn.getId(), txn.getWalletId(), txn.getType(),
                BigDecimal.valueOf(txn.getAmountCents(), 2), txn.getCurrency(), txn.getDescription(),
                txn.getReferenceType(), txn.getReferenceId(), txn.getCreatedAt());
    }

    private int toCents(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
