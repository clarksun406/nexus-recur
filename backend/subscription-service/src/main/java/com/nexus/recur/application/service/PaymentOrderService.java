package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.*;
import com.nexus.recur.domain.repository.PaymentOrderRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class PaymentOrderService {

    private static final long APPROVAL_THRESHOLD_CENTS = 500_000;

    private final PaymentOrderRepository paymentOrderRepository;
    private final WalletService walletService;
    private final SanctionsScreeningService sanctionsScreeningService;
    private final IdGenerator idGenerator;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository,
                                WalletService walletService,
                                SanctionsScreeningService sanctionsScreeningService,
                                IdGenerator idGenerator) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.walletService = walletService;
        this.sanctionsScreeningService = sanctionsScreeningService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public PaymentOrder create(String merchantId, String walletId, String currency, long amountCents,
                                PayoutMethod method, String beneficiaryName, String beneficiaryAccount,
                                String beneficiaryBank, String beneficiaryCountry, String purpose) {
        Wallet wallet = walletService.getEntity(walletId);
        if (!wallet.getMerchantId().equals(merchantId)) {
            throw new BusinessException("FORBIDDEN", "Wallet does not belong to this merchant");
        }
        if (!wallet.getCurrency().equals(currency)) {
            throw new BusinessException("CURRENCY_MISMATCH", "Wallet currency does not match payment currency");
        }
        if (amountCents <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be positive");
        }

        walletService.freeze(walletId, amountCents);

        PaymentOrder order = new PaymentOrder();
        order.setId(idGenerator.next("po"));
        order.setMerchantId(merchantId);
        order.setWalletId(walletId);
        order.setCurrency(currency);
        order.setAmountCents(amountCents);
        order.setMethod(method);
        order.setBeneficiaryName(beneficiaryName);
        order.setBeneficiaryAccount(beneficiaryAccount);
        order.setBeneficiaryBank(beneficiaryBank);
        order.setBeneficiaryCountry(beneficiaryCountry);
        order.setPurpose(purpose);
        order.setReferenceNumber("PO-" + order.getId().substring(3).toUpperCase());

        SanctionsResult result = sanctionsScreeningService.screen(beneficiaryName, beneficiaryCountry);
        order.setSanctionsResult(result);
        order.setSanctionsCheckedAt(OffsetDateTime.now());

        if (result == SanctionsResult.blocked) {
            order.setStatus(PaymentOrderStatus.blocked);
            walletService.unfreeze(walletId, amountCents);
        } else if (amountCents > APPROVAL_THRESHOLD_CENTS) {
            order.setStatus(PaymentOrderStatus.pending_approval);
        } else {
            order.setStatus(PaymentOrderStatus.approved);
            executePayment(order);
        }

        return paymentOrderRepository.save(order);
    }

    @Transactional
    public PaymentOrder approve(String orderId, String approverId) {
        PaymentOrder order = getEntity(orderId);
        if (order.getStatus() != PaymentOrderStatus.pending_approval) {
            throw new BusinessException("INVALID_STATE", "Order is not pending approval");
        }
        order.setStatus(PaymentOrderStatus.approved);
        order.setApprovedBy(approverId);
        order.setApprovedAt(OffsetDateTime.now());
        executePayment(order);
        return paymentOrderRepository.save(order);
    }

    @Transactional
    public PaymentOrder reject(String orderId, String approverId) {
        PaymentOrder order = getEntity(orderId);
        if (order.getStatus() != PaymentOrderStatus.pending_approval) {
            throw new BusinessException("INVALID_STATE", "Order is not pending approval");
        }
        order.setStatus(PaymentOrderStatus.rejected);
        order.setApprovedBy(approverId);
        order.setApprovedAt(OffsetDateTime.now());
        walletService.unfreeze(order.getWalletId(), order.getAmountCents());
        return paymentOrderRepository.save(order);
    }

    @Transactional
    public PaymentOrder complete(String orderId) {
        PaymentOrder order = getEntity(orderId);
        if (order.getStatus() != PaymentOrderStatus.processing) {
            throw new BusinessException("INVALID_STATE", "Order is not processing");
        }
        order.setStatus(PaymentOrderStatus.completed);
        return paymentOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public PaymentOrder get(String orderId) {
        return getEntity(orderId);
    }

    @Transactional(readOnly = true)
    public Page<PaymentOrder> list(String merchantId, String status, int page, int limit) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null && !status.isBlank()) {
            return paymentOrderRepository.findByStatus(PaymentOrderStatus.valueOf(status), pageable);
        }
        if (merchantId != null && !merchantId.isBlank()) {
            return paymentOrderRepository.findByMerchantId(merchantId, pageable);
        }
        return paymentOrderRepository.findAll(pageable);
    }

    private void executePayment(PaymentOrder order) {
        walletService.settlePending(order.getWalletId(), order.getAmountCents());
        walletService.applyToWallet(order.getMerchantId(), order.getCurrency(),
                java.math.BigDecimal.valueOf(order.getAmountCents(), 2),
                WalletTransactionType.expense,
                "Payout " + order.getMethod() + " to " + order.getBeneficiaryName(),
                "payment_order", order.getId());
        order.setStatus(PaymentOrderStatus.processing);
    }

    private PaymentOrder getEntity(String orderId) {
        return paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Payment order not found"));
    }
}
