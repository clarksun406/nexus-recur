package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendDunningEmail(Subscription subscription, SubscriptionPlan plan, DunningService.DunningTemplate template) {
        log.info("Dunning email [{}] to user {} for subscription {} (plan: {}, retryCount: {})",
                template.getCode(), subscription.getUserId(), subscription.getId(),
                plan.getName(), subscription.getRetryCount());
    }

    public void sendTrialReminder(Subscription subscription, SubscriptionPlan plan, int daysRemaining) {
        log.info("Trial reminder to user {} for subscription {} ({} days remaining)",
                subscription.getUserId(), subscription.getId(), daysRemaining);
    }
}
