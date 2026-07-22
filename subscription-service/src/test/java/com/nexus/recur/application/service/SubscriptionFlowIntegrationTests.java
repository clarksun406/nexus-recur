package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.BillingCycle;
import com.nexus.recur.domain.model.InvoiceStatus;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.ProrationBehavior;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionStatus;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.domain.repository.SubscriptionInvoiceRepository;
import com.nexus.recur.domain.repository.SubscriptionPlanRepository;
import com.nexus.recur.domain.repository.SubscriptionRepository;
import com.nexus.recur.domain.repository.WalletRepository;
import com.nexus.recur.domain.repository.WalletTransactionRepository;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanRequest;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanResponse;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.CancelSubscriptionRequest;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.CreateSubscriptionRequest;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.CreateSubscriptionResponse;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.EntitlementResponse;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.PauseSubscriptionRequest;
import com.nexus.recur.interfaces.rest.dto.SubscriptionDtos.UpgradeSubscriptionRequest;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SubscriptionFlowIntegrationTests {
    @Autowired
    private PlanService planService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private SubscriptionPlanRepository planRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;
    @Autowired
    private SubscriptionEventRepository eventRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @BeforeEach
    void cleanDatabase() {
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        eventRepository.deleteAll();
        invoiceRepository.deleteAll();
        subscriptionRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    void createsUpdatesListsAndArchivesPlans() {
        PlanResponse created = createPlan("Pro", BigDecimal.valueOf(29.90));

        assertThat(created.id()).startsWith("plan_");
        assertThat(created.status()).isEqualTo(PlanStatus.active);
        assertThat(created.features()).containsEntry("max_api_calls", 10000);
        assertThat(planService.list(PlanStatus.active, 1, 20).total()).isEqualTo(1);

        PlanResponse updated = planService.update(created.id(), new PlanRequest(
                "Pro Plus",
                "updated",
                "prod_pro",
                BillingCycle.annual,
                BigDecimal.valueOf(299),
                "usd",
                14,
                Map.of("storage_gb", 100),
                PlanStatus.active,
                null, null, null, null, null, null
        ));

        assertThat(updated.name()).isEqualTo("Pro Plus");
        assertThat(updated.currency()).isEqualTo("USD");
        assertThat(updated.billingCycle()).isEqualTo(BillingCycle.annual);

        PlanResponse archived = planService.archive(created.id());
        assertThat(archived.status()).isEqualTo(PlanStatus.archived);
    }

    @Test
    void createsSubscriptionActivatesRenewsIdempotentlyAndChecksEntitlement() {
        PlanResponse plan = createPlan("Pro", BigDecimal.valueOf(29.90));
        CreateSubscriptionResponse checkout = subscriptionService.create(new CreateSubscriptionRequest(
                plan.id(),
                "user_1",
                "https://example.com/success",
                "https://example.com/cancel"
        ));
        assertThat(checkout.checkoutUrl()).contains(checkout.subscriptionId());
        assertThat(checkout.status()).isEqualTo(SubscriptionStatus.pending);

        Subscription subscription = subscriptionService.getEntity(checkout.subscriptionId());
        OffsetDateTime start = OffsetDateTime.parse("2026-07-13T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-08-13T00:00:00Z");
        subscriptionService.activate(subscription, start, end, "{}");

        Subscription activated = subscriptionService.getEntity(checkout.subscriptionId());
        assertThat(activated.getStatus()).isEqualTo(SubscriptionStatus.active);
        assertThat(activated.getCurrentPeriodEnd()).isEqualTo(end);

        OffsetDateTime renewalEnd = OffsetDateTime.parse("2026-09-13T00:00:00Z");
        subscriptionService.recordPaid(activated, end, renewalEnd, BigDecimal.valueOf(29.90), "USD", "card", "txn_1", "{}");
        subscriptionService.recordPaid(activated, end, renewalEnd, BigDecimal.valueOf(29.90), "USD", "card", "txn_1", "{}");

        assertThat(invoiceRepository.findAll()).hasSize(1);
        assertThat(invoiceRepository.findAll().get(0).getStatus()).isEqualTo(InvoiceStatus.paid);
        assertThat(subscriptionService.getEntity(checkout.subscriptionId()).getCurrentPeriodEnd()).isEqualTo(renewalEnd);

        var wallets = walletRepository.findByMerchantId("merchant_default");
        assertThat(wallets).hasSize(1);
        assertThat(wallets.get(0).getCurrency()).isEqualTo("USD");
        assertThat(wallets.get(0).getBalanceCents()).isEqualTo(2990);

        EntitlementResponse entitlement = entitlementService.check("user_1");
        assertThat(entitlement.entitled()).isTrue();
        assertThat(entitlement.subscriptionId()).isEqualTo(checkout.subscriptionId());
        assertThat(entitlement.features()).containsEntry("max_api_calls", 10000);
    }

    @Test
    void pausesResumesUpgradesAndCancelsSubscription() {
        PlanResponse basic = createPlan("Basic", BigDecimal.valueOf(10));
        PlanResponse pro = createPlan("Pro", BigDecimal.valueOf(30));
        CreateSubscriptionResponse checkout = subscriptionService.create(new CreateSubscriptionRequest(
                basic.id(),
                "user_2",
                "https://example.com/success",
                "https://example.com/cancel"
        ));
        Subscription subscription = subscriptionService.getEntity(checkout.subscriptionId());
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");

        assertThat(subscriptionService.pause(subscription.getId(), new PauseSubscriptionRequest("break", 30)).status()).isEqualTo(SubscriptionStatus.paused);
        assertThat(subscriptionService.resume(subscription.getId()).status()).isEqualTo(SubscriptionStatus.active);
        assertThat(subscriptionService.upgrade(subscription.getId(), new UpgradeSubscriptionRequest(pro.id(), null)).newPlan()).isEqualTo(pro.id());
        assertThat(subscriptionService.cancel(subscription.getId(), new CancelSubscriptionRequest(false, "too expensive")).status())
                .isEqualTo(SubscriptionStatus.scheduled_cancel);
    }

    @Test
    void upgradeWithProrationNoneChargesNothing() {
        PlanResponse basic = createPlan("Basic", BigDecimal.valueOf(10));
        PlanResponse pro = createPlan("Pro", BigDecimal.valueOf(30));
        CreateSubscriptionResponse checkout = subscriptionService.create(new CreateSubscriptionRequest(
                basic.id(), "user_proration_none", "https://example.com/success", "https://example.com/cancel"
        ));
        Subscription subscription = subscriptionService.getEntity(checkout.subscriptionId());
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");

        var result = subscriptionService.upgrade(subscription.getId(),
                new UpgradeSubscriptionRequest(pro.id(), ProrationBehavior.proration_none));

        assertThat(result.newPlan()).isEqualTo(pro.id());
        assertThat(result.oldPlan()).isEqualTo(basic.id());
        assertThat(result.prorationAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.prorationType()).isEqualTo("none");

        Subscription updated = subscriptionService.getEntity(subscription.getId());
        assertThat(updated.getPlanId()).isEqualTo(pro.id());
    }

    @Test
    void upgradeWithProrationChargeCalculatesDifference() {
        PlanResponse basic = createPlan("Basic", BigDecimal.valueOf(10));
        PlanResponse pro = createPlan("Pro", BigDecimal.valueOf(30));
        CreateSubscriptionResponse checkout = subscriptionService.create(new CreateSubscriptionRequest(
                basic.id(), "user_proration_charge", "https://example.com/success", "https://example.com/cancel"
        ));
        Subscription subscription = subscriptionService.getEntity(checkout.subscriptionId());
        subscriptionService.activate(subscription, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(29), "{}");

        var result = subscriptionService.upgrade(subscription.getId(),
                new UpgradeSubscriptionRequest(pro.id(), ProrationBehavior.proration_charge));

        assertThat(result.newPlan()).isEqualTo(pro.id());
        assertThat(result.prorationAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.prorationType()).isEqualTo("charge");
    }

    private PlanResponse createPlan(String name, BigDecimal price) {
        return planService.create(new PlanRequest(
                name,
                name + " plan",
                "prod_" + name.toLowerCase(),
                BillingCycle.monthly,
                price,
                "USD",
                0,
                Map.of("max_api_calls", 10000, "premium_support", true),
                PlanStatus.active,
                null, null, null, null, null, null
        ));
    }
}
