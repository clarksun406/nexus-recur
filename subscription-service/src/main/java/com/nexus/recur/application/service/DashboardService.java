package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.InvoiceStatus;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionEvent;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.model.Wallet;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.repository.WalletRepository;
import com.nexus.recur.interfaces.rest.dto.DashboardDtos.DashboardStatsResponse;
import com.nexus.recur.interfaces.rest.dto.DashboardDtos.EventSummary;
import com.nexus.recur.interfaces.rest.dto.DashboardDtos.PendingAction;
import com.nexus.recur.interfaces.rest.dto.DashboardDtos.WalletBalance;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final SubscriptionEventRepository eventRepository;
    private final WalletRepository walletRepository;
    private final PlanService planService;

    public DashboardService(SubscriptionRepository subscriptionRepository,
                            SubscriptionInvoiceRepository invoiceRepository,
                            SubscriptionEventRepository eventRepository,
                            WalletRepository walletRepository,
                            PlanService planService) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.eventRepository = eventRepository;
        this.walletRepository = walletRepository;
        this.planService = planService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        Map<String, Integer> byStatus = new LinkedHashMap<>();
        int total = 0;
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            long count = subscriptionRepository.countByStatus(status);
            byStatus.put(status.name(), (int) count);
            total += (int) count;
        }
        int active = byStatus.getOrDefault(SubscriptionStatus.active.name(), 0);
        int trialing = byStatus.getOrDefault(SubscriptionStatus.trialing.name(), 0);

        BigDecimal mrr = calculateMrr();

        long paidInvoices = invoiceRepository.countByStatus(InvoiceStatus.paid);
        long failedInvoices = invoiceRepository.countByStatus(InvoiceStatus.failed);
        int totalCharges = (int) (paidInvoices + failedInvoices);
        BigDecimal successRate = totalCharges > 0
                ? BigDecimal.valueOf(paidInvoices).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalCharges), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, BigDecimal> revenueByCurrency = calculateRevenueByCurrency();

        List<WalletBalance> walletBalances = walletRepository.findAll().stream()
                .map(w -> new WalletBalance(w.getCurrency(),
                        BigDecimal.valueOf(w.getBalanceCents(), 2),
                        BigDecimal.valueOf(w.getPendingBalanceCents(), 2)))
                .toList();

        List<PendingAction> pendingActions = collectPendingActions();

        List<EventSummary> recentEvents = eventRepository.findAll(
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(e -> new EventSummary(e.getId(), e.getSubscriptionId(), e.getEventType(), e.getSource(), e.getCreatedAt()))
                .toList();

        return new DashboardStatsResponse(active, trialing, total, byStatus, mrr, "USD",
                successRate, totalCharges, revenueByCurrency, walletBalances, pendingActions, recentEvents);
    }

    private BigDecimal calculateMrr() {
        List<Subscription> subs = new ArrayList<>();
        subs.addAll(subscriptionRepository.findByStatus(SubscriptionStatus.active, PageRequest.of(0, 500)).getContent());
        subs.addAll(subscriptionRepository.findByStatus(SubscriptionStatus.trialing, PageRequest.of(0, 500)).getContent());

        BigDecimal mrr = BigDecimal.ZERO;
        for (Subscription sub : subs) {
            SubscriptionPlan plan = planService.getEntity(sub.getPlanId());
            BigDecimal monthlyPrice = normalizeToMonthly(plan.getBillingCycle(), BigDecimal.valueOf(plan.getPriceCents(), 2));
            mrr = mrr.add(monthlyPrice);
        }
        return mrr.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeToMonthly(BillingCycle cycle, BigDecimal price) {
        return switch (cycle) {
            case monthly -> price;
            case quarterly -> price.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case six_month -> price.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
            case annual -> price.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        };
    }

    private Map<String, BigDecimal> calculateRevenueByCurrency() {
        Map<String, BigDecimal> revenue = new LinkedHashMap<>();
        invoiceRepository.findAll().stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.paid)
                .forEach(inv -> revenue.merge(inv.getCurrency(),
                        BigDecimal.valueOf(inv.getAmountCents(), 2),
                        BigDecimal::add));
        return revenue;
    }

    private List<PendingAction> collectPendingActions() {
        List<PendingAction> actions = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime sevenDaysAhead = now.plusDays(7);

        subscriptionRepository.findByStatus(SubscriptionStatus.past_due, PageRequest.of(0, 50))
                .forEach(s -> actions.add(new PendingAction(s.getId(), s.getUserId(), s.getStatus().name(), s.getCurrentPeriodEnd(), "retry_charge")));

        subscriptionRepository.findByStatus(SubscriptionStatus.scheduled_cancel, PageRequest.of(0, 50))
                .getContent().stream()
                .filter(s -> s.getCurrentPeriodEnd() != null && !s.getCurrentPeriodEnd().isAfter(sevenDaysAhead))
                .forEach(s -> actions.add(new PendingAction(s.getId(), s.getUserId(), s.getStatus().name(), s.getCurrentPeriodEnd(), "cancel_at_period_end")));

        subscriptionRepository.findByStatus(SubscriptionStatus.paused, PageRequest.of(0, 50))
                .forEach(s -> actions.add(new PendingAction(s.getId(), s.getUserId(), s.getStatus().name(), s.getCurrentPeriodEnd(), "paused_follow_up")));

        return actions;
    }
}
