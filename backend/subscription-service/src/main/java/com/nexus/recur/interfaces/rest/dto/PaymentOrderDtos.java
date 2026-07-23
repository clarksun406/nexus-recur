package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.PayoutMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;

public final class PaymentOrderDtos {
    private PaymentOrderDtos() {}

    public record CreatePaymentOrderRequest(
            @NotBlank String merchantId,
            @NotBlank String walletId,
            @NotBlank String currency,
            @Positive long amountCents,
            @NotNull PayoutMethod method,
            @NotBlank String beneficiaryName,
            @NotBlank String beneficiaryAccount,
            String beneficiaryBank,
            String beneficiaryCountry,
            String purpose
    ) {}

    public record PaymentOrderResponse(
            String id,
            String merchantId,
            String walletId,
            String currency,
            long amountCents,
            String method,
            String beneficiaryName,
            String beneficiaryAccount,
            String beneficiaryBank,
            String beneficiaryCountry,
            String purpose,
            String status,
            String sanctionsResult,
            String approvedBy,
            String referenceNumber,
            OffsetDateTime createdAt
    ) {}

    public record ApprovalRequest(@NotBlank String approverId) {}
}
