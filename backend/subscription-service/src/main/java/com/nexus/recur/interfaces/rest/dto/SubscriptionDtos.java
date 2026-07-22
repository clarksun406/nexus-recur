package com.nexus.recur.interfaces.rest.dto;

import com.nexus.recur.domain.model.InvoiceStatus;
import com.nexus.recur.domain.model.ProrationBehavior;
import com.nexus.recur.domain.model.SubscriptionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public final class SubscriptionDtos {
    private SubscriptionDtos() {}

    public record CreateSubscriptionRequest(@NotBlank String planId, @NotBlank String userId, @NotBlank String successUrl, @NotBlank String cancelUrl) {}
    public record CreateSubscriptionResponse(String subscriptionId, String checkoutUrl, SubscriptionStatus status) {}

    public record SubscriptionResponse(
            String id,
            String userId,
            String planId,
            String externalSubId,
            String externalCustomerId,
            SubscriptionStatus status,
            OffsetDateTime currentPeriodStart,
            OffsetDateTime currentPeriodEnd,
            OffsetDateTime trialEndAt,
            boolean cancelAtPeriodEnd,
            OffsetDateTime canceledAt,
            String cancelReason,
            OffsetDateTime pausedAt,
            Map<String, Object> metadata,
            int retryCount,
            String lastDeclineCode,
            String paymentMethodId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    public record CancelSubscriptionRequest(boolean immediate, String reason) {}
    public record CancelSubscriptionResponse(String subscriptionId, SubscriptionStatus status, boolean cancelAtPeriodEnd) {}
    public record PauseSubscriptionRequest(String reason, @Min(1) Integer maxPauseDays) {}
    public record UpgradeSubscriptionRequest(@NotBlank String newPlanId, ProrationBehavior prorationBehavior) {}
    public record UpgradeSubscriptionResponse(String subscriptionId, String oldPlan, String newPlan, BigDecimal prorationAmount, String prorationType) {}

    public record InvoiceResponse(
            String id,
            String subscriptionId,
            String externalTransactionId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            BigDecimal amount,
            String currency,
            InvoiceStatus status,
            String paymentMethod,
            OffsetDateTime paidAt,
            OffsetDateTime createdAt
    ) {}

    public record EntitlementResponse(String userId, boolean entitled, String reason, String subscriptionId, OffsetDateTime validUntil, Map<String, Object> features) {}

    public record EventResponse(String id, String subscriptionId, String eventType, String source, OffsetDateTime createdAt) {}
}
