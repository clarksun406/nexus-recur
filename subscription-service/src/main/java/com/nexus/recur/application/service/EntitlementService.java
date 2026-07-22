package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.infrastructure.support.JsonService;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.EntitlementResponse;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntitlementService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;
    private final JsonService jsonService;

    public EntitlementService(SubscriptionRepository subscriptionRepository, PlanService planService, JsonService jsonService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planService = planService;
        this.jsonService = jsonService;
    }

    @Transactional(readOnly = true)
    public EntitlementResponse check(String userId) {
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.active, PageRequest.of(0, 20))
                .stream()
                .max(Comparator.comparing(Subscription::getCurrentPeriodEnd, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        if (subscription == null) {
            return new EntitlementResponse(userId, false, "no active subscription", null, null, Map.of());
        }
        if (subscription.getCurrentPeriodEnd() != null && subscription.getCurrentPeriodEnd().isBefore(OffsetDateTime.now())) {
            return new EntitlementResponse(userId, false, "subscription expired", subscription.getId(), subscription.getCurrentPeriodEnd(), Map.of());
        }
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());
        return new EntitlementResponse(userId, true, "active", subscription.getId(), subscription.getCurrentPeriodEnd(), jsonService.read(plan.getFeaturesJson()));
    }
}
