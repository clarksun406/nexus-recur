package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.VirtualCard;
import com.nexus.recur.domain.model.VirtualCardStatus;
import com.nexus.recur.domain.repository.VirtualCardRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.VirtualCardDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
public class VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;
    private final IdGenerator idGenerator;
    private final SecureRandom random = new SecureRandom();

    public VirtualCardService(VirtualCardRepository virtualCardRepository, IdGenerator idGenerator) {
        this.virtualCardRepository = virtualCardRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public VirtualCardResponse issue(String merchantId, IssueCardRequest request) {
        VirtualCard card = new VirtualCard();
        card.setId(idGenerator.next("vc"));
        card.setMerchantId(merchantId);
        card.setCustomerId(request.customerId());
        card.setCardToken("tok_" + generateToken());
        card.setLast4(generateLast4());
        card.setExpMonth(request.expMonth() != null ? request.expMonth() : 12);
        card.setExpYear(request.expYear() != null ? request.expYear() : 2029);
        card.setCurrency(request.currency() != null ? request.currency().toUpperCase() : "USD");
        card.setSpendingLimitCents(request.spendingLimitCents() != null ? request.spendingLimitCents() : 100_000);
        card.setSpentCents(0);
        card.setLabel(request.label());
        if (request.validityMonths() != null && request.validityMonths() > 0) {
            card.setExpiresAt(OffsetDateTime.now().plusMonths(request.validityMonths()));
        }
        virtualCardRepository.save(card);
        return toResponse(card);
    }

    @Transactional(readOnly = true)
    public Page<VirtualCardResponse> list(String merchantId, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return virtualCardRepository.findByMerchantId(merchantId, pageRequest).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public VirtualCardResponse get(String cardId) {
        return toResponse(getEntity(cardId));
    }

    @Transactional
    public VirtualCardResponse freeze(String cardId) {
        VirtualCard card = getEntity(cardId);
        if (card.getStatus() != VirtualCardStatus.active) {
            throw new BusinessException("INVALID_CARD_STATE", "only active cards can be frozen");
        }
        card.setStatus(VirtualCardStatus.frozen);
        card.setFrozenAt(OffsetDateTime.now());
        virtualCardRepository.save(card);
        return toResponse(card);
    }

    @Transactional
    public VirtualCardResponse unfreeze(String cardId) {
        VirtualCard card = getEntity(cardId);
        if (card.getStatus() != VirtualCardStatus.frozen) {
            throw new BusinessException("INVALID_CARD_STATE", "only frozen cards can be unfrozen");
        }
        card.setStatus(VirtualCardStatus.active);
        card.setFrozenAt(null);
        virtualCardRepository.save(card);
        return toResponse(card);
    }

    @Transactional
    public VirtualCardResponse close(String cardId) {
        VirtualCard card = getEntity(cardId);
        if (card.getStatus() == VirtualCardStatus.closed) {
            throw new BusinessException("INVALID_CARD_STATE", "card already closed");
        }
        card.setStatus(VirtualCardStatus.closed);
        card.setClosedAt(OffsetDateTime.now());
        virtualCardRepository.save(card);
        return toResponse(card);
    }

    @Transactional
    public VirtualCardResponse recordSpend(String cardId, long amountCents) {
        VirtualCard card = getEntity(cardId);
        if (card.getStatus() != VirtualCardStatus.active) {
            throw new BusinessException("CARD_NOT_ACTIVE", "cannot spend on non-active card");
        }
        if (card.getSpentCents() + amountCents > card.getSpendingLimitCents()) {
            throw new BusinessException("SPENDING_LIMIT_EXCEEDED", "transaction exceeds card spending limit");
        }
        card.setSpentCents(card.getSpentCents() + amountCents);
        virtualCardRepository.save(card);
        return toResponse(card);
    }

    private VirtualCard getEntity(String cardId) {
        return virtualCardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("VIRTUAL_CARD_NOT_FOUND", "virtual card not found: " + cardId));
    }

    private String generateToken() {
        StringBuilder sb = new StringBuilder(24);
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 24; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateLast4() {
        return String.format("%04d", random.nextInt(10000));
    }

    private VirtualCardResponse toResponse(VirtualCard c) {
        return new VirtualCardResponse(c.getId(), c.getMerchantId(), c.getCustomerId(),
                c.getCardToken(), c.getLast4(), c.getExpMonth(), c.getExpYear(),
                c.getCurrency(), c.getSpendingLimitCents(), c.getSpentCents(),
                c.getLabel(), c.getStatus(), c.getIssuedAt(), c.getExpiresAt(),
                c.getFrozenAt(), c.getClosedAt());
    }
}
