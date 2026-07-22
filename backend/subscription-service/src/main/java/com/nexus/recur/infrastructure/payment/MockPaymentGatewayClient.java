package com.nexus.recur.infrastructure.payment;

import com.nexus.recur.application.port.PaymentGatewayClient;
import com.nexus.recur.config.SubscriptionProperties;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "payment-gateway", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockPaymentGatewayClient implements PaymentGatewayClient {
    private final SubscriptionProperties properties;

    public MockPaymentGatewayClient(SubscriptionProperties properties) {
        this.properties = properties;
    }

    @Override
    public CheckoutSession createCheckoutSession(Subscription subscription, SubscriptionPlan plan, String successUrl, String cancelUrl) {
        String externalSubId = "ext_" + subscription.getId();
        String externalCustomerId = "cus_" + subscription.getUserId();
        String checkoutUrl = UriComponentsBuilder.fromUriString(properties.getCheckoutBaseUrl())
                .path("/checkout/")
                .path(subscription.getId())
                .queryParam("plan", plan.getId())
                .queryParam("success_url", successUrl)
                .queryParam("cancel_url", cancelUrl)
                .build()
                .toUriString();
        return new CheckoutSession(checkoutUrl, externalSubId, externalCustomerId);
    }

    @Override
    public ChargeResult charge(ChargeCommand command) {
        String intentId = "pi_mock_" + UUID.randomUUID();
        String txId = "txn_mock_" + UUID.randomUUID();
        return new ChargeResult(true, intentId, txId, null, false);
    }

    @Override
    public RefundResult refund(String paymentIntentId, long amountCents, String currency, String reason) {
        return new RefundResult(true, "re_mock_" + UUID.randomUUID());
    }
}
