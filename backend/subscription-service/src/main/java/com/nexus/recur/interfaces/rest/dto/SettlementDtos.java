package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.SettlementStatus;

import java.time.OffsetDateTime;

public class SettlementDtos {

    public record InitiateSettlementRequest(
            String walletId,
            long amountCents,
            String targetCurrency,
            String bankAccount,
            String backgroundRefs
    ) {}

    public record ApproveSettlementRequest(String approverId) {}

    public record RejectSettlementRequest(String approverId, String reason) {}

    public record SettlementResponse(
            String id,
            String merchantId,
            String walletId,
            long amountCents,
            String currency,
            String targetCurrency,
            String bankAccount,
            SettlementStatus status,
            String approvedBy,
            OffsetDateTime approvedAt,
            String backgroundRefs,
            String rejectionReason,
            OffsetDateTime createdAt,
            OffsetDateTime completedAt
    ) {}
}
