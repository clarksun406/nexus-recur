package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.Refund;
import com.nexus.recur.domain.model.RefundStatus;
import com.nexus.recur.domain.repository.RefundRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.RefundDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final IdGenerator idGenerator;

    public RefundService(RefundRepository refundRepository, IdGenerator idGenerator) {
        this.refundRepository = refundRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public RefundResponse create(CreateRefundRequest request) {
        Refund refund = new Refund();
        refund.setId(idGenerator.next("ref"));
        refund.setInvoiceId(request.invoiceId());
        refund.setSubscriptionId(request.subscriptionId());
        refund.setMerchantId(request.merchantId());
        refund.setAmountCents(request.amountCents());
        refund.setCurrency(request.currency());
        refund.setReason(request.reason());
        refund.setInitiatedBy(request.initiatedBy());
        refundRepository.save(refund);
        return toResponse(refund);
    }

    @Transactional
    public RefundResponse approve(String refundId, String approvedBy) {
        Refund refund = getEntity(refundId);
        if (refund.getStatus() != RefundStatus.pending) {
            throw new BusinessException("INVALID_STATE", "Refund is not pending approval");
        }
        refund.setStatus(RefundStatus.approved);
        refund.setApprovedBy(approvedBy);
        refund.setProcessedAt(OffsetDateTime.now());
        refundRepository.save(refund);
        return toResponse(refund);
    }

    @Transactional(readOnly = true)
    public RefundResponse get(String refundId) {
        return toResponse(getEntity(refundId));
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> list(String merchantId, int page, int limit) {
        return refundRepository.findByMerchantId(merchantId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)))
                .map(this::toResponse);
    }

    private Refund getEntity(String refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("REFUND_NOT_FOUND", "Refund not found: " + refundId));
    }

    private RefundResponse toResponse(Refund r) {
        return new RefundResponse(r.getId(), r.getInvoiceId(), r.getSubscriptionId(),
                r.getMerchantId(), r.getAmountCents(), r.getCurrency(), r.getReason(),
                r.getStatus(), r.getInitiatedBy(), r.getApprovedBy(), r.getProcessedAt(), r.getCreatedAt());
    }
}
