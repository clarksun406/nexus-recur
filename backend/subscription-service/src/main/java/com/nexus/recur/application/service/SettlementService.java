package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.*;
import com.nexus.recur.domain.repository.SettlementRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.SettlementDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SettlementService {

    private static final long APPROVAL_THRESHOLD_CENTS = 1_000_000; // $10,000

    private final SettlementRepository settlementRepository;
    private final WalletService walletService;
    private final IdGenerator idGenerator;
    private final EventService eventService;

    public SettlementService(SettlementRepository settlementRepository,
                             WalletService walletService,
                             IdGenerator idGenerator,
                             EventService eventService) {
        this.settlementRepository = settlementRepository;
        this.walletService = walletService;
        this.idGenerator = idGenerator;
        this.eventService = eventService;
    }

    @Transactional
    public SettlementResponse initiate(String merchantId, InitiateSettlementRequest request) {
        Wallet wallet = walletService.getEntity(request.walletId());
        if (!wallet.getMerchantId().equals(merchantId)) {
            throw new BusinessException("WALLET_NOT_OWNED", "wallet does not belong to this merchant");
        }
        if (wallet.getBalanceCents() < request.amountCents()) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "wallet balance is insufficient for this settlement");
        }
        if (request.amountCents() <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "settlement amount must be positive");
        }

        Settlement settlement = new Settlement();
        settlement.setId(idGenerator.next("stl"));
        settlement.setMerchantId(merchantId);
        settlement.setWalletId(request.walletId());
        settlement.setAmountCents(request.amountCents());
        settlement.setCurrency(wallet.getCurrency());
        settlement.setTargetCurrency(request.targetCurrency());
        settlement.setBankAccount(request.bankAccount());
        settlement.setBackgroundRefs(request.backgroundRefs());

        boolean requiresApproval = request.amountCents() > APPROVAL_THRESHOLD_CENTS;
        settlement.setStatus(requiresApproval ? SettlementStatus.pending : SettlementStatus.approved);

        if (!requiresApproval) {
            settlement.setApprovedBy("auto");
            settlement.setApprovedAt(OffsetDateTime.now());
        }

        settlementRepository.save(settlement);

        walletService.freeze(request.walletId(), request.amountCents());

        eventService.record(null, "settlement_initiated", "api",
                "{\"settlementId\":\"" + settlement.getId() + "\",\"amount\":" + request.amountCents() + "}");

        return toResponse(settlement);
    }

    @Transactional
    public SettlementResponse approve(String settlementId, String approverId) {
        Settlement settlement = getEntity(settlementId);
        if (settlement.getStatus() != SettlementStatus.pending) {
            throw new BusinessException("INVALID_STATE", "settlement is not pending approval");
        }
        settlement.setStatus(SettlementStatus.approved);
        settlement.setApprovedBy(approverId);
        settlement.setApprovedAt(OffsetDateTime.now());
        settlementRepository.save(settlement);
        return toResponse(settlement);
    }

    @Transactional
    public SettlementResponse reject(String settlementId, String approverId, String reason) {
        Settlement settlement = getEntity(settlementId);
        if (settlement.getStatus() != SettlementStatus.pending) {
            throw new BusinessException("INVALID_STATE", "settlement is not pending approval");
        }
        settlement.setStatus(SettlementStatus.rejected);
        settlement.setApprovedBy(approverId);
        settlement.setRejectionReason(reason);
        settlementRepository.save(settlement);

        walletService.unfreeze(settlement.getWalletId(), settlement.getAmountCents());

        return toResponse(settlement);
    }

    @Transactional
    public SettlementResponse complete(String settlementId) {
        Settlement settlement = getEntity(settlementId);
        if (settlement.getStatus() != SettlementStatus.approved && settlement.getStatus() != SettlementStatus.processing) {
            throw new BusinessException("INVALID_STATE", "settlement cannot be completed from state: " + settlement.getStatus());
        }
        settlement.setStatus(SettlementStatus.completed);
        settlement.setCompletedAt(OffsetDateTime.now());
        settlementRepository.save(settlement);
        walletService.settlePending(settlement.getWalletId(), settlement.getAmountCents());
        return toResponse(settlement);
    }

    @Transactional(readOnly = true)
    public PageResult<SettlementResponse> list(String merchantId, SettlementStatus status, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1));
        Page<Settlement> result;
        if (status != null) {
            result = settlementRepository.findByMerchantIdAndStatus(merchantId, status, pageRequest);
        } else {
            result = settlementRepository.findByMerchantId(merchantId, pageRequest);
        }
        return new PageResult<>(result.map(this::toResponse).getContent(), page, limit, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SettlementResponse get(String settlementId) {
        return toResponse(getEntity(settlementId));
    }

    private Settlement getEntity(String settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException("SETTLEMENT_NOT_FOUND", "settlement not found: " + settlementId));
    }

    private SettlementResponse toResponse(Settlement s) {
        return new SettlementResponse(s.getId(), s.getMerchantId(), s.getWalletId(),
                s.getAmountCents(), s.getCurrency(), s.getTargetCurrency(), s.getBankAccount(),
                s.getStatus(), s.getApprovedBy(), s.getApprovedAt(), s.getBackgroundRefs(),
                s.getRejectionReason(), s.getCreatedAt(), s.getCompletedAt());
    }
}
