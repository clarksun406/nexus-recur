package com.nexus.recur.application.service;

import com.nexus.recur.application.port.PaymentGatewayClient;
import com.nexus.recur.domain.model.*;
import com.nexus.recur.domain.repository.InvoiceLineItemRepository;
import com.nexus.recur.domain.repository.PlanTierRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.repository.UsageRecordRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class UsageBillingService {

    private static final Logger log = LoggerFactory.getLogger(UsageBillingService.class);
    private static final double ANOMALY_MULTIPLIER = 3.0;

    private final SubscriptionRepository subscriptionRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final PlanTierRepository planTierRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final PlanService planService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final EventService eventService;
    private final IdGenerator idGenerator;

    public UsageBillingService(SubscriptionRepository subscriptionRepository,
                               UsageRecordRepository usageRecordRepository,
                               PlanTierRepository planTierRepository,
                               SubscriptionInvoiceRepository invoiceRepository,
                               InvoiceLineItemRepository lineItemRepository,
                               PlanService planService,
                               PaymentGatewayClient paymentGatewayClient,
                               EventService eventService,
                               IdGenerator idGenerator) {
        this.subscriptionRepository = subscriptionRepository;
        this.usageRecordRepository = usageRecordRepository;
        this.planTierRepository = planTierRepository;
        this.invoiceRepository = invoiceRepository;
        this.lineItemRepository = lineItemRepository;
        this.planService = planService;
        this.paymentGatewayClient = paymentGatewayClient;
        this.eventService = eventService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public void settleUsage(Subscription subscription) {
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());
        if (plan.getBillingType() == BillingType.flat_rate) {
            return;
        }

        OffsetDateTime periodStart = subscription.getCurrentPeriodStart();
        OffsetDateTime periodEnd = subscription.getCurrentPeriodEnd();
        if (periodStart == null || periodEnd == null) return;

        List<UsageRecord> records = usageRecordRepository
                .findBySubscriptionIdAndRecordedAtBetween(subscription.getId(), periodStart, periodEnd);
        if (records.isEmpty()) {
            log.info("No usage records for subscription {} in period, skipping", subscription.getId());
            return;
        }

        long totalQuantity = records.stream().mapToLong(UsageRecord::getQuantity).sum();

        if (isAnomalous(subscription.getId(), periodStart, totalQuantity)) {
            eventService.record(subscription.getId(), "usage_anomaly_detected", "scheduler",
                    "{\"quantity\":" + totalQuantity + ",\"periodStart\":\"" + periodStart + "\"}");
            log.warn("Anomalous usage detected for subscription {}: {} units", subscription.getId(), totalQuantity);
        }

        long chargeCents = calculateCharge(plan, totalQuantity);
        if (chargeCents <= 0) {
            log.info("Zero usage charge for subscription {}, skipping", subscription.getId());
            return;
        }

        SubscriptionInvoice invoice = createUsageInvoice(subscription, plan, periodStart, periodEnd,
                totalQuantity, chargeCents);

        PaymentGatewayClient.ChargeResult result = paymentGatewayClient.charge(
                new PaymentGatewayClient.ChargeCommand(
                        subscription.getMerchantId() != null ? subscription.getMerchantId() : "merchant_default",
                        chargeCents,
                        plan.getCurrency(),
                        "Usage charge: " + totalQuantity + " units for " + subscription.getId(),
                        subscription.getId(),
                        invoice.getId(),
                        subscription.getPaymentMethodId()
                ));

        if (result.success()) {
            invoice.setStatus(InvoiceStatus.paid);
            invoice.setPaidAt(OffsetDateTime.now());
            invoice.setPaymentIntentId(result.paymentIntentId());
            invoiceRepository.save(invoice);
            eventService.record(subscription.getId(), "usage_charge_succeeded", "scheduler",
                    "{\"amount\":" + chargeCents + ",\"quantity\":" + totalQuantity + "}");
            log.info("Usage charge succeeded for subscription {}: {} cents for {} units",
                    subscription.getId(), chargeCents, totalQuantity);
        } else {
            invoice.setStatus(InvoiceStatus.failed);
            invoiceRepository.save(invoice);
            eventService.record(subscription.getId(), "usage_charge_failed", "scheduler",
                    "{\"declineCode\":\"" + result.declineCode() + "\"}");
            log.warn("Usage charge failed for subscription {}: {}", subscription.getId(), result.declineCode());
        }
    }

    long calculateCharge(SubscriptionPlan plan, long totalQuantity) {
        if (plan.getBillingType() == BillingType.tiered) {
            return calculateTieredCharge(plan.getId(), totalQuantity);
        }
        // metered: flat per-unit price from plan.priceCents
        return plan.getPriceCents() * totalQuantity;
    }

    private long calculateTieredCharge(String planId, long totalQuantity) {
        List<PlanTier> tiers = planTierRepository.findByPlanIdOrderByTierStartAsc(planId);
        if (tiers.isEmpty()) return 0;

        long totalCharge = 0;
        long remaining = totalQuantity;

        for (PlanTier tier : tiers) {
            if (remaining <= 0) break;

            int tierStart = tier.getTierStart();
            Integer tierEnd = tier.getTierEnd();
            long tierCapacity = (tierEnd != null) ? (long) tierEnd - tierStart + 1 : Long.MAX_VALUE;
            long unitsInTier = Math.min(remaining, tierCapacity);

            totalCharge += tier.getFlatAmountCents() + (unitsInTier * tier.getUnitAmountCents());
            remaining -= unitsInTier;
        }

        return totalCharge;
    }

    private boolean isAnomalous(String subscriptionId, OffsetDateTime currentPeriodStart, long currentQuantity) {
        OffsetDateTime prevPeriodStart = currentPeriodStart.minusMonths(1);
        List<UsageRecord> prevRecords = usageRecordRepository
                .findBySubscriptionIdAndRecordedAtBetween(subscriptionId, prevPeriodStart, currentPeriodStart);
        if (prevRecords.isEmpty()) return false;

        long prevQuantity = prevRecords.stream().mapToLong(UsageRecord::getQuantity).sum();
        if (prevQuantity == 0) return false;

        return currentQuantity > prevQuantity * ANOMALY_MULTIPLIER;
    }

    private SubscriptionInvoice createUsageInvoice(Subscription subscription, SubscriptionPlan plan,
                                                   OffsetDateTime periodStart, OffsetDateTime periodEnd,
                                                   long quantity, long chargeCents) {
        SubscriptionInvoice invoice = new SubscriptionInvoice();
        invoice.setId(idGenerator.next("inv"));
        invoice.setSubscriptionId(subscription.getId());
        invoice.setPeriodStart(periodStart);
        invoice.setPeriodEnd(periodEnd);
        invoice.setAmountCents((int) chargeCents);
        invoice.setCurrency(plan.getCurrency());
        invoice.setStatus(InvoiceStatus.pending);
        invoice.setSubtotalCents((int) chargeCents);
        invoice.setTotalCents((int) chargeCents);
        invoice.setTaxAmountCents(0);
        invoice.setMerchantId(subscription.getMerchantId());
        invoice.setCustomerId(subscription.getCustomerId());
        invoiceRepository.save(invoice);

        InvoiceLineItem lineItem = new InvoiceLineItem();
        lineItem.setId(idGenerator.next("ili"));
        lineItem.setInvoiceId(invoice.getId());
        lineItem.setDescription("Usage: " + quantity + " units @ " + plan.getName());
        lineItem.setQuantity((int) quantity);
        lineItem.setUnitPriceCents(plan.getPriceCents());
        lineItem.setAmountCents((int) chargeCents);
        lineItem.setTaxAmountCents(0);
        lineItem.setPeriodStart(periodStart);
        lineItem.setPeriodEnd(periodEnd);
        lineItem.setPlanId(plan.getId());
        lineItem.setLineType(LineItemType.subscription);
        lineItemRepository.save(lineItem);

        return invoice;
    }
}
