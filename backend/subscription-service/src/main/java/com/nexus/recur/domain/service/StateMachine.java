package com.nexus.recur.domain.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.SubscriptionStatus;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StateMachine {
    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> ALLOWED = Map.of(
            SubscriptionStatus.pending, Set.of(SubscriptionStatus.trialing, SubscriptionStatus.active, SubscriptionStatus.canceled, SubscriptionStatus.expired),
            SubscriptionStatus.trialing, Set.of(SubscriptionStatus.active, SubscriptionStatus.past_due, SubscriptionStatus.paused, SubscriptionStatus.scheduled_cancel, SubscriptionStatus.canceled, SubscriptionStatus.expired),
            SubscriptionStatus.active, Set.of(SubscriptionStatus.past_due, SubscriptionStatus.paused, SubscriptionStatus.scheduled_cancel, SubscriptionStatus.canceled, SubscriptionStatus.expired),
            SubscriptionStatus.past_due, Set.of(SubscriptionStatus.active, SubscriptionStatus.canceled, SubscriptionStatus.expired),
            SubscriptionStatus.paused, Set.of(SubscriptionStatus.active, SubscriptionStatus.canceled, SubscriptionStatus.expired),
            SubscriptionStatus.scheduled_cancel, Set.of(SubscriptionStatus.active, SubscriptionStatus.canceled, SubscriptionStatus.expired),
            SubscriptionStatus.canceled, Set.of(),
            SubscriptionStatus.expired, Set.of()
    );

    public void ensureAllowed(SubscriptionStatus from, SubscriptionStatus to) {
        if (from == to) {
            return;
        }
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION", "cannot transition subscription from " + from + " to " + to);
        }
    }
}
