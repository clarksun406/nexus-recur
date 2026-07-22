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
    private final EventService eventService;

    public SubscriptionScheduler(SubscriptionRepository subscriptionRepository,
                                  SubscriptionService subscriptionService,
                                  EventService eventService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
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
}
