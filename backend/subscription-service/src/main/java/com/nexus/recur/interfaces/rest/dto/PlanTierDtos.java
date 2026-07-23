package com.nexus.recur.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class PlanTierDtos {
    private PlanTierDtos() {}

    public record CreateTierRequest(
            @NotBlank String planId,
            @Min(0) int tierStart,
            Integer tierEnd,
            @Min(0) int unitAmountCents,
            @Min(0) int flatAmountCents
    ) {}

    public record TierResponse(
            String id,
            String planId,
            int tierStart,
            Integer tierEnd,
            int unitAmountCents,
            int flatAmountCents
    ) {}
}
