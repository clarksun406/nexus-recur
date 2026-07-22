package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.*;
import com.nexus.recur.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BillingServiceTest {

    @Autowired
    private BillingService billingService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionPlanRepository planRepository;
    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;
    @Autowired
    private SubscriptionEventRepository eventRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        invoiceRepository.deleteAll();
        subscriptionRepository.deleteAll();
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        planRepository.deleteAll();

        plan = new SubscriptionPlan();
        plan.setId("plan_billing_test");
        plan.setName("Pro");
        plan.setPriceCents(2999);
        plan.setCurrency("USD");
        plan.setBillingCycle(BillingCycle.monthly);
        plan.setStatus(PlanStatus.active);
        planRepository.save(plan);
    }

    @Test
    void renewalSuccess_advancesPeriod() {
        Subscription sub = createSubscription(SubscriptionStatus.active,
                OffsetDateTime.now().minusDays(1));

        billingService.chargeRenewal(sub);

        Subscription updated = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.active);
        assertThat(updated.getCurrentPeriodEnd()).isAfter(OffsetDateTime.now());
        assertThat(updated.getRetryCount()).isZero();
    }

    @Test
    void renewalSuccess_createsInvoice() {
        Subscription sub = createSubscription(SubscriptionStatus.active,
                OffsetDateTime.now().minusDays(1));

        billingService.chargeRenewal(sub);

        var invoices = invoiceRepository.findBySubscriptionId(sub.getId(),
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(invoices.getTotalElements()).isEqualTo(1);
        assertThat(invoices.getContent().get(0).getStatus()).isEqualTo(InvoiceStatus.paid);
        assertThat(invoices.getContent().get(0).getAmountCents()).isEqualTo(2999);
    }

    @Test
    void trialConversion_success_activatesSubscription() {
        Subscription sub = createSubscription(SubscriptionStatus.trialing, null);
        sub.setTrialEndAt(OffsetDateTime.now().minusDays(1));
        subscriptionRepository.save(sub);

        billingService.chargeTrialConversion(sub);

        Subscription updated = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.active);
        assertThat(updated.getCurrentPeriodEnd()).isAfter(OffsetDateTime.now());
    }

    @Test
    void retrySuccess_reactivatesSubscription() {
        Subscription sub = createSubscription(SubscriptionStatus.past_due,
                OffsetDateTime.now().minusDays(5));
        sub.setRetryCount(2);
        sub.setLastDeclineCode("network_timeout");
        sub.setNextRetryAt(OffsetDateTime.now().minusHours(1));
        subscriptionRepository.save(sub);

        billingService.retryCharge(sub);

        Subscription updated = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SubscriptionStatus.active);
        assertThat(updated.getRetryCount()).isZero();
        assertThat(updated.getLastDeclineCode()).isNull();
        assertThat(updated.getNextRetryAt()).isNull();
    }

    private Subscription createSubscription(SubscriptionStatus status, OffsetDateTime periodEnd) {
        Subscription sub = new Subscription();
        sub.setId("sub_bill_" + System.nanoTime());
        sub.setUserId("user_billing");
        sub.setPlanId(plan.getId());
        sub.setStatus(status);
        sub.setCurrentPeriodStart(periodEnd != null ? periodEnd.minusDays(30) : OffsetDateTime.now().minusDays(30));
        sub.setCurrentPeriodEnd(periodEnd);
        sub.setPaymentMethodId("pm_test");
        return subscriptionRepository.save(sub);
    }
}
