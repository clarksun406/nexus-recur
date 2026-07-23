package com.nexus.recur.application.service;

import com.nexus.recur.application.port.PaymentGatewayClient;
import com.nexus.recur.domain.model.RetryLog;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.repository.RetryLogRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private static final int[] RETRY_SCHEDULE_DAYS = {1, 3, 7, 14};
    private static final int MAX_RETRIES = RETRY_SCHEDULE_DAYS.length;

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final PlanService planService;
    private final SubscriptionService subscriptionService;
    private final EventService eventService;
    private final RetryLogRepository retryLogRepository;
    private final IdGenerator idGenerator;

    public BillingService(SubscriptionRepository subscriptionRepository,
                          PaymentGatewayClient paymentGatewayClient,
                          PlanService planService,
                          SubscriptionService subscriptionService,
                          EventService eventService,
                          RetryLogRepository retryLogRepository,
                          IdGenerator idGenerator) {
        this.subscriptionRepository = subscriptionRepository;
        this.paymentGatewayClient = paymentGatewayClient;
        this.planService = planService;
        this.subscriptionService = subscriptionService;
        this.eventService = eventService;
        this.retryLogRepository = retryLogRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public void chargeRenewal(Subscription subscription) {
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());
        OffsetDateTime periodStart = subscription.getCurrentPeriodEnd();
        OffsetDateTime periodEnd = plan.getBillingCycle().addTo(periodStart);

        PaymentGatewayClient.ChargeResult result = executeCharge(subscription, plan);

        if (result.success()) {
            subscriptionService.recordPaid(subscription, periodStart, periodEnd,
                    BigDecimal.valueOf(plan.getPriceCents(), 2), plan.getCurrency(),
                    "card", result.providerTransactionId(), null);
            subscription.setRetryCount(0);
            subscription.setLastDeclineCode(null);
            subscription.setNextRetryAt(null);
            subscriptionRepository.save(subscription);
            log.info("Renewal charge succeeded for subscription {}", subscription.getId());
        } else {
            handleChargeFailure(subscription, result);
        }
    }

    @Transactional
    public void chargeTrialConversion(Subscription subscription) {
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());
        OffsetDateTime periodStart = OffsetDateTime.now();
        OffsetDateTime periodEnd = plan.getBillingCycle().addTo(periodStart);

        PaymentGatewayClient.ChargeResult result = executeCharge(subscription, plan);

        if (result.success()) {
            subscriptionService.recordPaid(subscription, periodStart, periodEnd,
                    BigDecimal.valueOf(plan.getPriceCents(), 2), plan.getCurrency(),
                    "card", result.providerTransactionId(), null);
            subscription.setRetryCount(0);
            subscription.setLastDeclineCode(null);
            subscription.setNextRetryAt(null);
            subscriptionRepository.save(subscription);
            log.info("Trial conversion charge succeeded for subscription {}", subscription.getId());
        } else {
            handleChargeFailure(subscription, result);
        }
    }

    @Transactional
    public void retryCharge(Subscription subscription) {
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());

        PaymentGatewayClient.ChargeResult result = executeCharge(subscription, plan);

        if (result.success()) {
            OffsetDateTime periodStart = subscription.getCurrentPeriodEnd() != null
                    ? subscription.getCurrentPeriodEnd() : OffsetDateTime.now();
            OffsetDateTime periodEnd = plan.getBillingCycle().addTo(periodStart);
            subscriptionService.recordPaid(subscription, periodStart, periodEnd,
                    BigDecimal.valueOf(plan.getPriceCents(), 2), plan.getCurrency(),
                    "card", result.providerTransactionId(), null);
            subscription.setRetryCount(0);
            subscription.setLastDeclineCode(null);
            subscription.setNextRetryAt(null);
            subscriptionRepository.save(subscription);
            log.info("Retry charge succeeded for subscription {}", subscription.getId());
        } else {
            handleChargeFailure(subscription, result);
        }
    }

    private PaymentGatewayClient.ChargeResult executeCharge(Subscription subscription, SubscriptionPlan plan) {
        PaymentGatewayClient.ChargeCommand command = new PaymentGatewayClient.ChargeCommand(
                "merchant_default",
                plan.getPriceCents(),
                plan.getCurrency(),
                "Subscription renewal: " + subscription.getId(),
                subscription.getId(),
                null,
                subscription.getPaymentMethodId()
        );
        return paymentGatewayClient.charge(command);
    }

    private void handleChargeFailure(Subscription subscription, PaymentGatewayClient.ChargeResult result) {
        int newRetryCount = subscription.getRetryCount() + 1;
        subscription.setRetryCount(newRetryCount);
        subscription.setLastDeclineCode(result.declineCode());

        RetryLog retryLog = new RetryLog();
        retryLog.setId(idGenerator.next("rtl"));
        retryLog.setSubscriptionId(subscription.getId());
        retryLog.setAttemptNumber(newRetryCount);
        retryLog.setAttemptedAt(OffsetDateTime.now());
        retryLog.setDeclineCode(result.declineCode());

        if (!result.retryable() || newRetryCount >= MAX_RETRIES) {
            subscriptionService.markStatus(subscription, SubscriptionStatus.canceled, null);
            subscription.setNextRetryAt(null);
            subscriptionRepository.save(subscription);
            retryLogRepository.save(retryLog);
            log.warn("Subscription {} canceled after {} retries (declineCode={})",
                    subscription.getId(), newRetryCount, result.declineCode());
            eventService.record(subscription.getId(), "canceled_after_retries", "scheduler", null);
        } else {
            if (subscription.getStatus() != SubscriptionStatus.past_due) {
                subscriptionService.markStatus(subscription, SubscriptionStatus.past_due, null);
            }
            int delayDays = RETRY_SCHEDULE_DAYS[Math.min(newRetryCount, RETRY_SCHEDULE_DAYS.length - 1)];
            subscription.setNextRetryAt(OffsetDateTime.now().plusDays(delayDays));
            subscriptionRepository.save(subscription);
            retryLog.setNextRetryAt(subscription.getNextRetryAt());
            retryLogRepository.save(retryLog);
            log.info("Subscription {} marked past_due, retry #{} scheduled in {} days",
                    subscription.getId(), newRetryCount, delayDays);
            eventService.record(subscription.getId(), "charge_failed", "scheduler", null);
        }
    }
}
