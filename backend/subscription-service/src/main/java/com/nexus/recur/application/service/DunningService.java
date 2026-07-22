package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DunningService {

    private static final Logger log = LoggerFactory.getLogger(DunningService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;
    private final EventService eventService;
    private final NotificationService notificationService;

    public DunningService(SubscriptionRepository subscriptionRepository,
                          PlanService planService,
                          EventService eventService,
                          NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.planService = planService;
        this.eventService = eventService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void processDunningSequence() {
        List<Subscription> pastDue = subscriptionRepository
                .findByStatusAndNextRetryAtBefore(com.nexus.recur.domain.model.SubscriptionStatus.past_due, OffsetDateTime.now().plusDays(1));

        for (Subscription sub : pastDue) {
            int retryCount = sub.getRetryCount();
            SubscriptionPlan plan = planService.getEntity(sub.getPlanId());

            if (retryCount == 1) {
                notificationService.sendDunningEmail(sub, plan, DunningTemplate.PAYMENT_FAILED);
                eventService.record(sub.getId(), "dunning_email_1_sent", "scheduler", null);
            } else if (retryCount == 2) {
                notificationService.sendDunningEmail(sub, plan, DunningTemplate.LAST_CHANCE);
                eventService.record(sub.getId(), "dunning_email_2_sent", "scheduler", null);
            } else if (retryCount == 3) {
                notificationService.sendDunningEmail(sub, plan, DunningTemplate.CANCELLATION_NOTICE);
                eventService.record(sub.getId(), "dunning_email_3_sent", "scheduler", null);
            }
        }
    }

    public enum DunningTemplate {
        PAYMENT_FAILED("payment_failed", "Your payment could not be processed"),
        LAST_CHANCE("last_chance", "Action required: update your payment method"),
        CANCELLATION_NOTICE("cancellation_notice", "Your subscription will be canceled");

        private final String code;
        private final String subject;

        DunningTemplate(String code, String subject) {
            this.code = code;
            this.subject = subject;
        }

        public String getCode() { return code; }
        public String getSubject() { return subject; }
    }
}
