package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.WalletStatus;
import com.nexus.recur.domain.model.WalletTransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class WalletDtos {

    public record WalletResponse(String id, String merchantId, String currency, BigDecimal balance,
                                  BigDecimal pendingBalance, WalletStatus status,
                                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {}

    public record TransactionResponse(String id, String walletId, WalletTransactionType type,
                                       BigDecimal amount, String currency, String description,
                                       String referenceType, String referenceId, OffsetDateTime createdAt) {}

    public record RecordTransactionRequest(@NotNull WalletTransactionType type,
                                           @NotNull @Min(0) BigDecimal amount,
                                           @NotBlank String currency,
                                           String description,
                                           String referenceType,
                                           String referenceId) {}
}
