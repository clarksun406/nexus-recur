package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.VirtualCardStatus;

import java.time.OffsetDateTime;

public class VirtualCardDtos {

    public record IssueCardRequest(
            String customerId,
            String currency,
            Long spendingLimitCents,
            String label,
            Integer expMonth,
            Integer expYear,
            Integer validityMonths
    ) {}

    public record SpendRequest(
            Long amountCents
    ) {}

    public record VirtualCardResponse(
            String id,
            String merchantId,
            String customerId,
            String cardToken,
            String last4,
            int expMonth,
            int expYear,
            String currency,
            long spendingLimitCents,
            long spentCents,
            String label,
            VirtualCardStatus status,
            OffsetDateTime issuedAt,
            OffsetDateTime expiresAt,
            OffsetDateTime frozenAt,
            OffsetDateTime closedAt
    ) {}
}
