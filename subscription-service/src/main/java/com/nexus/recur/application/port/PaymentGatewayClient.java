package com.nexus.recur.application.port;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;

public interface PaymentGatewayClient {
    CheckoutSession createCheckoutSession(Subscription subscription, SubscriptionPlan plan, String successUrl, String cancelUrl);

    ChargeResult charge(ChargeCommand command);

    RefundResult refund(String paymentIntentId, long amountCents, String currency, String reason);

    record CheckoutSession(String checkoutUrl, String externalSubId, String externalCustomerId) {}

    record ChargeCommand(String merchantId, long amountCents, String currency, String description,
                         String subscriptionId, String invoiceId, String paymentMethodId) {}

    record ChargeResult(boolean success, String paymentIntentId, String providerTransactionId,
                        String declineCode, boolean retryable) {}

    record RefundResult(boolean success, String refundId) {}
}
