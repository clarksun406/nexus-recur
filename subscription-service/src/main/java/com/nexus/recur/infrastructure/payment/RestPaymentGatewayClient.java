package com.nexus.recur.infrastructure.payment;

import com.nexus.recur.application.port.PaymentGatewayClient;
import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "payment-gateway", name = "enabled", havingValue = "true")
public class RestPaymentGatewayClient implements PaymentGatewayClient {

    private final RestClient restClient;
    private final String baseUrl;

    public RestPaymentGatewayClient(@Value("${payment-gateway.base-url:http://localhost:8081}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public CheckoutSession createCheckoutSession(Subscription subscription, SubscriptionPlan plan, String successUrl, String cancelUrl) {
        Map<String, Object> body = Map.of(
                "merchantId", "merchant_default",
                "amountCents", plan.getPriceCents(),
                "currency", plan.getCurrency(),
                "description", "Subscription checkout: " + plan.getName(),
                "metadata", "{\"subscriptionId\":\"" + subscription.getId() + "\"}",
                "paymentMethodId", subscription.getPaymentMethodId() != null ? subscription.getPaymentMethodId() : ""
        );

        Map<?, ?> response = restClient.post()
                .uri("/v1/payments/intents")
                .body(body)
                .retrieve()
                .body(Map.class);

        String intentId = (String) response.get("id");
        String checkoutUrl = baseUrl + "/v1/payments/intents/" + intentId + "/confirm";
        return new CheckoutSession(checkoutUrl, intentId, "cus_" + subscription.getUserId());
    }

    @Override
    public ChargeResult charge(ChargeCommand command) {
        Map<String, Object> createBody = Map.of(
                "merchantId", command.merchantId(),
                "amountCents", command.amountCents(),
                "currency", command.currency(),
                "description", command.description() != null ? command.description() : "",
                "metadata", buildMetadata(command),
                "paymentMethodId", command.paymentMethodId() != null ? command.paymentMethodId() : ""
        );

        Map<?, ?> created = restClient.post()
                .uri("/v1/payments/intents")
                .body(createBody)
                .retrieve()
                .body(Map.class);

        String intentId = (String) created.get("id");

        Map<?, ?> confirmed = restClient.post()
                .uri("/v1/payments/intents/{id}/confirm", intentId)
                .retrieve()
                .body(Map.class);

        boolean success = "succeeded".equals(confirmed.get("status"));
        String providerTxId = (String) confirmed.get("providerTransactionId");
        String declineCode = (String) confirmed.get("declineCode");
        Boolean retryable = (Boolean) confirmed.get("retryable");

        return new ChargeResult(success, intentId, providerTxId, declineCode, Boolean.TRUE.equals(retryable));
    }

    @Override
    public RefundResult refund(String paymentIntentId, long amountCents, String currency, String reason) {
        Map<String, Object> body = Map.of(
                "paymentIntentId", paymentIntentId,
                "amountCents", amountCents,
                "currency", currency,
                "reason", reason != null ? reason : ""
        );

        Map<?, ?> response = restClient.post()
                .uri("/v1/payments/refunds")
                .body(body)
                .retrieve()
                .body(Map.class);

        boolean success = "succeeded".equals(response.get("status"));
        String refundId = (String) response.get("id");
        return new RefundResult(success, refundId);
    }

    private String buildMetadata(ChargeCommand command) {
        StringBuilder sb = new StringBuilder("{");
        if (command.subscriptionId() != null) {
            sb.append("\"subscriptionId\":\"").append(command.subscriptionId()).append("\"");
        }
        if (command.invoiceId() != null) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"invoiceId\":\"").append(command.invoiceId()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
}
