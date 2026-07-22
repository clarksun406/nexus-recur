package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubscriptionScheduler {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final BillingService billingService;
    private final EventService eventService;

    public SubscriptionScheduler(SubscriptionRepository subscriptionRepository,
                                  SubscriptionService subscriptionService,
                                  BillingService billingService,
                                  EventService eventService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.billingService = billingService;
        this.eventService = eventService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void executeScheduledCancellations() {
        List<Subscription> expired = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.scheduled_cancel, OffsetDateTime.now());
        if (expired.isEmpty()) return;
        log.info("processing {} scheduled_cancel subscriptions past period end", expired.size());
        for (Subscription subscription : expired) {
            subscriptionService.markStatus(subscription, SubscriptionStatus.canceled, null);
        }
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void processRenewals() {
        List<Subscription> due = subscriptionRepository
                .findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus.active, OffsetDateTime.now());
        if (due.isEmpty()) return;
        log.info("processing {} active subscriptions past period end for renewal", due.size());
        for (Subscription subscription : due) {
            try {
                billingService.chargeRenewal(subscription);
            } catch (Exception e) {
                log.error("Renewal failed for subscription {}: {}", subscription.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void processTrialConversions() {
        List<Subscription> expired = subscriptionRepository
                .findByStatusAndTrialEndAtBefore(SubscriptionStatus.trialing, OffsetDateTime.now());
        if (expired.isEmpty()) return;
        log.info("processing {} trialing subscriptions past trial end", expired.size());
        for (Subscription subscription : expired) {
            try {
                billingService.chargeTrialConversion(subscription);
            } catch (Exception e) {
                log.error("Trial conversion failed for subscription {}: {}", subscription.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 15 0 * * *")
    @Transactional
    public void processRetries() {
        List<Subscription> due = subscriptionRepository
                .findByStatusAndNextRetryAtBefore(SubscriptionStatus.past_due, OffsetDateTime.now());
        if (due.isEmpty()) return;
        log.info("processing {} past_due subscriptions for retry", due.size());
        for (Subscription subscription : due) {
            try {
                billingService.retryCharge(subscription);
            } catch (Exception e) {
                log.error("Retry failed for subscription {}: {}", subscription.getId(), e.getMessage());
            }
        }
    }
}
