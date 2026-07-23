package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.RoutingStrategy;

import java.time.OffsetDateTime;

public class RoutingDtos {

    public record CreateRoutingRuleRequest(
            String providerName,
            Integer priority,
            String currency,
            Long minAmountCents,
            Long maxAmountCents,
            String region,
            Integer weight,
            RoutingStrategy strategy,
            Double successRate,
            Double costPercentage
    ) {}

    public record UpdateRoutingRuleRequest(
            String providerName,
            Integer priority,
            String currency,
            Long minAmountCents,
            Long maxAmountCents,
            String region,
            Integer weight,
            Boolean active,
            RoutingStrategy strategy,
            Double successRate,
            Double costPercentage
    ) {}

    public record ResolveRequest(
            String currency,
            Long amountCents,
            String region
    ) {}

    public record RoutingRuleResponse(
            String id,
            String merchantId,
            String providerName,
            int priority,
            String currency,
            Long minAmountCents,
            Long maxAmountCents,
            String region,
            int weight,
            boolean active,
            RoutingStrategy strategy,
            double successRate,
            double costPercentage,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    public record RoutingDecision(
            String provider,
            String reason,
            String ruleId
    ) {}
}
