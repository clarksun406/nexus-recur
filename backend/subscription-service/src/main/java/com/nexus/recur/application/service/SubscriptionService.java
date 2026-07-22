package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.application.port.PaymentGatewayClient;
import com.nexus.recur.domain.model.InvoiceStatus;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.ProrationBehavior;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionEvent;
import com.nexus.recur.domain.model.SubscriptionInvoice;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.model.WalletTransactionType;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.service.StateMachine;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.infrastructure.support.JsonService;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final SubscriptionEventRepository eventRepository;
    private final PlanService planService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final IdGenerator idGenerator;
    private final JsonService jsonService;
    private final StateMachine stateMachine;
    private final EventService eventService;
    private final WalletService walletService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionInvoiceRepository invoiceRepository,
                               SubscriptionEventRepository eventRepository, PlanService planService,
                               PaymentGatewayClient paymentGatewayClient, IdGenerator idGenerator, JsonService jsonService,
                               StateMachine stateMachine, EventService eventService, WalletService walletService) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
        this.eventRepository = eventRepository;
        this.planService = planService;
        this.paymentGatewayClient = paymentGatewayClient;
        this.idGenerator = idGenerator;
        this.jsonService = jsonService;
        this.stateMachine = stateMachine;
        this.eventService = eventService;
        this.walletService = walletService;
    }

    @Transactional
    public CreateSubscriptionResponse create(CreateSubscriptionRequest request) {
        SubscriptionPlan plan = planService.getEntity(request.planId());
        if (plan.getStatus() != PlanStatus.active) {
            throw new BusinessException("PLAN_NOT_ACTIVE", "subscription plan is not active");
        }
        Subscription subscription = new Subscription();
        subscription.setId(idGenerator.next("sub"));
        subscription.setPlanId(plan.getId());
        subscription.setUserId(request.userId());
        subscription.setStatus(SubscriptionStatus.pending);
        subscriptionRepository.save(subscription);
        PaymentGatewayClient.CheckoutSession checkout = paymentGatewayClient.createCheckoutSession(subscription, plan, request.successUrl(), request.cancelUrl());
        subscription.setExternalSubId(checkout.externalSubId());
        subscription.setExternalCustomerId(checkout.externalCustomerId());
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), "created", "api", null);
        return new CreateSubscriptionResponse(subscription.getId(), checkout.checkoutUrl(), subscription.getStatus());
    }

    @Transactional(readOnly = true)
    public PageResult<SubscriptionResponse> list(String userId, SubscriptionStatus status, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1));
        Page<Subscription> result;
        if (userId != null && status != null) {
            result = subscriptionRepository.findByUserIdAndStatus(userId, status, pageRequest);
        } else if (userId != null) {
            result = subscriptionRepository.findByUserId(userId, pageRequest);
        } else if (status != null) {
            result = subscriptionRepository.findByStatus(status, pageRequest);
        } else {
            result = subscriptionRepository.findAll(pageRequest);
        }
        return new PageResult<>(result.map(this::toResponse).getContent(), page, limit, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get(String subscriptionId) { return toResponse(getEntity(subscriptionId)); }

    @Transactional
    public CancelSubscriptionResponse cancel(String subscriptionId, CancelSubscriptionRequest request) {
        Subscription subscription = getEntity(subscriptionId);
        if (request.immediate()) {
            transition(subscription, SubscriptionStatus.canceled);
            subscription.setCanceledAt(OffsetDateTime.now());
            subscription.setCancelAtPeriodEnd(false);
        } else {
            transition(subscription, SubscriptionStatus.scheduled_cancel);
            subscription.setCancelAtPeriodEnd(true);
        }
        subscription.setCancelReason(request.reason());
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), request.immediate() ? "canceled" : "scheduled_cancel", "api", null);
        return new CancelSubscriptionResponse(subscription.getId(), subscription.getStatus(), subscription.isCancelAtPeriodEnd());
    }

    @Transactional
    public SubscriptionResponse pause(String subscriptionId, PauseSubscriptionRequest request) {
        Subscription subscription = getEntity(subscriptionId);
        transition(subscription, SubscriptionStatus.paused);
        subscription.setPausedAt(OffsetDateTime.now());
        subscription.setMetadataJson(jsonService.write(java.util.Map.of(
                "pause_reason", request.reason() == null ? "" : request.reason(),
                "max_pause_days", request.maxPauseDays() == null ? 90 : request.maxPauseDays()
        )));
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), "paused", "api", null);
        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse resume(String subscriptionId) {
        Subscription subscription = getEntity(subscriptionId);
        transition(subscription, SubscriptionStatus.active);
        subscription.setPausedAt(null);
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), "resumed", "api", null);
        return toResponse(subscription);
    }

    @Transactional
    public UpgradeSubscriptionResponse upgrade(String subscriptionId, UpgradeSubscriptionRequest request) {
        Subscription subscription = getEntity(subscriptionId);
        SubscriptionPlan oldPlan = planService.getEntity(subscription.getPlanId());
        SubscriptionPlan newPlan = planService.getEntity(request.newPlanId());
        if (newPlan.getStatus() != PlanStatus.active) {
            throw new BusinessException("PLAN_NOT_ACTIVE", "new plan is not active");
        }
        ProrationBehavior behavior = request.prorationBehavior() == null ? ProrationBehavior.proration_charge_immediately : request.prorationBehavior();
        BigDecimal proration = BigDecimal.ZERO;
        String prorationType = "none";
        if (behavior != ProrationBehavior.proration_none) {
            proration = calculateProration(subscription, oldPlan, newPlan);
            prorationType = proration.signum() >= 0 ? "charge" : "credit";
        }
        subscription.setPlanId(newPlan.getId());
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), "plan_changed", "api", null);
        return new UpgradeSubscriptionResponse(subscription.getId(), oldPlan.getId(), newPlan.getId(), proration.abs(), prorationType);
    }

    @Transactional(readOnly = true)
    public PageResult<InvoiceResponse> invoices(String subscriptionId, int page, int limit) {
        Page<SubscriptionInvoice> result = invoiceRepository.findBySubscriptionId(subscriptionId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)));
        return new PageResult<>(result.map(this::toInvoiceResponse).getContent(), page, limit, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResult<EventResponse> events(String subscriptionId, int page, int limit) {
        Page<SubscriptionEvent> result = eventRepository.findBySubscriptionId(subscriptionId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)));
        return new PageResult<>(result.map(e -> new EventResponse(e.getId(), e.getSubscriptionId(), e.getEventType(), e.getSource(), e.getCreatedAt())).getContent(), page, limit, result.getTotalElements());
    }

    @Transactional
    public void activate(Subscription subscription, OffsetDateTime periodStart, OffsetDateTime periodEnd, String rawPayload) {
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());
        OffsetDateTime start = periodStart == null ? OffsetDateTime.now() : periodStart;
        OffsetDateTime end = periodEnd == null ? plan.getBillingCycle().addTo(start) : periodEnd;
        transition(subscription, plan.getTrialDays() > 0 ? SubscriptionStatus.trialing : SubscriptionStatus.active);
        if (plan.getTrialDays() > 0) {
            subscription.setTrialEndAt(start.plusDays(plan.getTrialDays()));
        }
        subscription.setCurrentPeriodStart(start);
        subscription.setCurrentPeriodEnd(end);
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), subscription.getStatus() == SubscriptionStatus.trialing ? "trialing" : "activated", "webhook", rawPayload);
    }

    @Transactional
    public void markStatus(Subscription subscription, SubscriptionStatus status, String rawPayload) {
        transition(subscription, status);
        if (status == SubscriptionStatus.canceled || status == SubscriptionStatus.expired) {
            subscription.setCanceledAt(OffsetDateTime.now());
            subscription.setCancelAtPeriodEnd(false);
        }
        if (status == SubscriptionStatus.paused) {
            subscription.setPausedAt(OffsetDateTime.now());
        }
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), status.name(), "webhook", rawPayload);
    }

    @Transactional
    public void recordPaid(Subscription subscription, OffsetDateTime periodStart, OffsetDateTime periodEnd, BigDecimal amount, String currency, String paymentMethod, String externalTransactionId, String rawPayload) {
        SubscriptionPlan plan = planService.getEntity(subscription.getPlanId());
        OffsetDateTime start = periodStart == null ? (subscription.getCurrentPeriodEnd() == null ? OffsetDateTime.now() : subscription.getCurrentPeriodEnd()) : periodStart;
        OffsetDateTime end = periodEnd == null ? plan.getBillingCycle().addTo(start) : periodEnd;
        if (!invoiceRepository.existsBySubscriptionIdAndPeriodStartAndPeriodEnd(subscription.getId(), start, end)) {
            SubscriptionInvoice invoice = new SubscriptionInvoice();
            invoice.setId(idGenerator.next("inv"));
            invoice.setSubscriptionId(subscription.getId());
            invoice.setPeriodStart(start);
            invoice.setPeriodEnd(end);
            invoice.setAmountCents(toCents(amount == null ? BigDecimal.valueOf(plan.getPriceCents(), 2) : amount));
            String invoiceCurrency = currency == null || currency.isBlank() ? plan.getCurrency() : currency.toUpperCase();
            invoice.setCurrency(invoiceCurrency);
            invoice.setStatus(InvoiceStatus.paid);
            invoice.setPaymentMethod(paymentMethod);
            invoice.setExternalTransactionId(externalTransactionId);
            invoice.setPaidAt(OffsetDateTime.now());
            invoiceRepository.save(invoice);
            walletService.applyToWallet("merchant_default", invoiceCurrency,
                    BigDecimal.valueOf(invoice.getAmountCents(), 2), WalletTransactionType.income,
                    "subscription charge", "invoice", invoice.getId());
        }
        if (subscription.getStatus() != SubscriptionStatus.active) {
            transition(subscription, SubscriptionStatus.active);
        }
        subscription.setCurrentPeriodStart(start);
        subscription.setCurrentPeriodEnd(end);
        subscriptionRepository.save(subscription);
        eventService.record(subscription.getId(), "renewed", "webhook", rawPayload);
    }

    public Subscription getEntity(String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException("SUBSCRIPTION_NOT_FOUND", "subscription not found: " + subscriptionId));
    }

    public Subscription findByLocalOrExternalId(String subscriptionId, String externalSubId) {
        if (subscriptionId != null && !subscriptionId.isBlank()) {
            return getEntity(subscriptionId);
        }
        if (externalSubId != null && !externalSubId.isBlank()) {
            return subscriptionRepository.findByExternalSubId(externalSubId)
                    .orElseThrow(() -> new BusinessException("SUBSCRIPTION_NOT_FOUND", "subscription not found by external id: " + externalSubId));
        }
        throw new BusinessException("SUBSCRIPTION_ID_REQUIRED", "subscriptionId or externalSubId is required");
    }

    public SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getUserId(), subscription.getPlanId(), subscription.getExternalSubId(),
                subscription.getExternalCustomerId(), subscription.getStatus(), subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd(),
                subscription.getTrialEndAt(), subscription.isCancelAtPeriodEnd(), subscription.getCanceledAt(), subscription.getCancelReason(),
                subscription.getPausedAt(), jsonService.read(subscription.getMetadataJson()),
                subscription.getRetryCount(), subscription.getLastDeclineCode(), subscription.getPaymentMethodId(),
                subscription.getCreatedAt(), subscription.getUpdatedAt());
    }

    private void transition(Subscription subscription, SubscriptionStatus target) {
        stateMachine.ensureAllowed(subscription.getStatus(), target);
        subscription.setStatus(target);
    }

    private BigDecimal calculateProration(Subscription subscription, SubscriptionPlan oldPlan, SubscriptionPlan newPlan) {
        if (subscription.getCurrentPeriodStart() == null || subscription.getCurrentPeriodEnd() == null) {
            return BigDecimal.valueOf(newPlan.getPriceCents() - oldPlan.getPriceCents(), 2);
        }
        long totalSeconds = Math.max(Duration.between(subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd()).toSeconds(), 1);
        long remainingSeconds = Math.max(Duration.between(OffsetDateTime.now(), subscription.getCurrentPeriodEnd()).toSeconds(), 0);
        BigDecimal ratio = BigDecimal.valueOf(remainingSeconds).divide(BigDecimal.valueOf(totalSeconds), 6, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(newPlan.getPriceCents() - oldPlan.getPriceCents(), 2).multiply(ratio).setScale(2, RoundingMode.HALF_UP);
    }

    private InvoiceResponse toInvoiceResponse(SubscriptionInvoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getSubscriptionId(), invoice.getExternalTransactionId(), invoice.getPeriodStart(),
                invoice.getPeriodEnd(), BigDecimal.valueOf(invoice.getAmountCents(), 2), invoice.getCurrency(), invoice.getStatus(),
                invoice.getPaymentMethod(), invoice.getPaidAt(), invoice.getCreatedAt());
    }

    private int toCents(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }
}
