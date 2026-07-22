package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.domain.repository.SubscriptionPlanRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SubscriptionSchedulerTest {
    @Autowired
    private SubscriptionScheduler scheduler;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionPlanRepository planRepository;
    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;
    @Autowired
    private SubscriptionEventRepository eventRepository;
    @Autowired
    private SubscriptionService subscriptionService;

    @BeforeEach
    void cleanUp() {
        eventRepository.deleteAll();
        invoiceRepository.deleteAll();
        subscriptionRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    void cancelsExpiredScheduledCancelSubscriptions() {
        Subscription subscription = createExpiredScheduledCancelSubscription();

        scheduler.executeScheduledCancellations();

        Subscription updated = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.canceled);
        assertThat(updated.getCanceledAt()).isNotNull();
    }

    @Test
    void doesNotCancelNotYetExpiredSubscriptions() {
        Subscription subscription = createScheduledCancelSubscription(OffsetDateTime.now().plusDays(5));

        scheduler.executeScheduledCancellations();

        Subscription updated = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.scheduled_cancel);
    }

    private Subscription createExpiredScheduledCancelSubscription() {
        return createScheduledCancelSubscription(OffsetDateTime.now().minusDays(1));
    }

    private Subscription createScheduledCancelSubscription(OffsetDateTime periodEnd) {
        Subscription subscription = new Subscription();
        subscription.setId("sub_sched_" + System.nanoTime());
        subscription.setUserId("user_sched");
        subscription.setPlanId("plan_dummy");
        subscription.setStatus(SubscriptionStatus.scheduled_cancel);
        subscription.setCurrentPeriodStart(OffsetDateTime.now().minusDays(30));
        subscription.setCurrentPeriodEnd(periodEnd);
        subscription.setCancelAtPeriodEnd(true);
        return subscriptionRepository.save(subscription);
    }
}
