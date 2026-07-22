package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.BillingType;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.TaxMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public final class PlanDtos {
    private PlanDtos() {}

    public record PlanRequest(
            @NotBlank String name,
            String description,
            String productId,
            @NotNull BillingCycle billingCycle,
            @NotNull @Min(0) BigDecimal price,
            String currency,
            @Min(0) Integer trialDays,
            Map<String, Object> features,
            PlanStatus status,
            BillingType billingType,
            Map<String, Object> meteredConfig,
            TaxMode taxMode,
            String taxCategory,
            Boolean licenseEnabled,
            @Min(1) Integer licenseInstanceLimit
    ) {}

    public record PlanResponse(
            String id,
            String name,
            String description,
            String productId,
            BillingCycle billingCycle,
            BigDecimal price,
            String currency,
            int trialDays,
            Map<String, Object> features,
            PlanStatus status,
            BillingType billingType,
            Map<String, Object> meteredConfig,
            TaxMode taxMode,
            String taxCategory,
            boolean licenseEnabled,
            int licenseInstanceLimit,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}
}
