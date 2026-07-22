package com.nexus.gateway.engine;

import com.nexus.gateway.config.GatewayProperties;
import com.nexus.gateway.domain.*;
import com.nexus.gateway.provider.*;
import com.nexus.gateway.service.WebhookNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrchestrator.class);

    private final PaymentIntentRepository intentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final RefundRepository refundRepository;
    private final PaymentProviderDispatcher dispatcher;
    private final RoutingEngine routingEngine;
    private final CircuitBreaker circuitBreaker;
    private final FailoverPolicy failoverPolicy;
    private final WebhookNotifier webhookNotifier;
    private final int maxAttempts;

    public PaymentOrchestrator(PaymentIntentRepository intentRepository,
                               PaymentAttemptRepository attemptRepository,
                               RefundRepository refundRepository,
                               PaymentProviderDispatcher dispatcher,
                               RoutingEngine routingEngine,
                               CircuitBreaker circuitBreaker,
                               FailoverPolicy failoverPolicy,
                               WebhookNotifier webhookNotifier,
                               GatewayProperties properties) {
        this.intentRepository = intentRepository;
        this.attemptRepository = attemptRepository;
        this.refundRepository = refundRepository;
        this.dispatcher = dispatcher;
        this.routingEngine = routingEngine;
        this.circuitBreaker = circuitBreaker;
        this.failoverPolicy = failoverPolicy;
        this.webhookNotifier = webhookNotifier;
        this.maxAttempts = properties.maxAttempts();
    }

    public PaymentIntent createIntent(String merchantId, long amountCents, String currency,
                                      String description, String metadata, String paymentMethodId) {
        String id = "pi_" + UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent(id, merchantId, amountCents, currency,
                description, metadata, paymentMethodId, maxAttempts);
        return intentRepository.save(intent);
    }

    public PaymentIntent confirm(String intentId) {
        PaymentIntent intent = intentRepository.findById(intentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found: " + intentId));

        if (!"requires_confirmation".equals(intent.getStatus())) {
            throw new IllegalStateException("Intent already processed: " + intent.getStatus());
        }

        intent.markProcessing();
        intentRepository.save(intent);

        List<PaymentProvider> candidates = routingEngine.resolve(intentId);
        ChargeResult lastResult = null;
        PaymentProvider lastProvider = null;

        for (PaymentProvider provider : candidates) {
            if (intent.getAttemptCount() >= intent.getMaxAttempts()) {
                break;
            }
            if (circuitBreaker.isOpen(provider)) {
                continue;
            }

            intent.incrementAttempts();
            ChargeRequest chargeRequest = new ChargeRequest(
                    intent.getId(), intent.getAmountCents(), intent.getCurrency(),
                    intent.getPaymentMethodId(), intent.getId() + "_" + intent.getAttemptCount(),
                    intent.getDescription()
            );

            ChargeResult result = dispatcher.charge(provider, chargeRequest);
            lastResult = result;
            lastProvider = provider;

            String attemptStatus = result.status() == ChargeResult.Status.SUCCEEDED ? "succeeded" : "failed";
            PaymentAttempt attempt = new PaymentAttempt(
                    "att_" + UUID.randomUUID(), intent.getId(), provider,
                    attemptStatus, result.providerTransactionId(), result.declineCode(), result.retryable()
            );
            attemptRepository.save(attempt);

            if (result.status() == ChargeResult.Status.SUCCEEDED) {
                circuitBreaker.recordSuccess(provider);
                intent.markSucceeded(result.providerTransactionId(), provider);
                intentRepository.save(intent);
                webhookNotifier.notifySuccess(intent);
                return intent;
            }

            boolean retryable = failoverPolicy.isRetryable(result.declineCode());
            circuitBreaker.recordFailure(provider, retryable);

            if (!retryable) {
                break;
            }
        }

        String declineCode = lastResult != null ? lastResult.declineCode() : "no_route_available";
        boolean retryable = lastResult != null && failoverPolicy.isRetryable(lastResult.declineCode());
        intent.markFailed(declineCode, retryable, lastProvider);
        intentRepository.save(intent);
        webhookNotifier.notifyFailure(intent);
        return intent;
    }

    public PaymentIntent getIntent(String intentId) {
        return intentRepository.findById(intentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found: " + intentId));
    }

    public PaymentIntent cancelIntent(String intentId) {
        PaymentIntent intent = getIntent(intentId);
        if ("succeeded".equals(intent.getStatus())) {
            throw new IllegalStateException("Cannot cancel a succeeded intent");
        }
        intent.markCanceled();
        return intentRepository.save(intent);
    }

    public Refund createRefund(String paymentIntentId, long amountCents, String currency, String reason) {
        PaymentIntent intent = getIntent(paymentIntentId);
        if (!"succeeded".equals(intent.getStatus())) {
            throw new IllegalStateException("Can only refund succeeded payments");
        }

        Refund refund = new Refund("re_" + UUID.randomUUID(), paymentIntentId, amountCents, currency, reason);
        RefundRequest request = new RefundRequest(intent.getProviderTransactionId(), amountCents, currency, reason);
        RefundResult result = dispatcher.refund(intent.getProvider(), request);

        if (result.status() == RefundResult.Status.SUCCEEDED) {
            refund.markSucceeded(result.providerRefundId());
        } else {
            refund.markFailed();
        }
        return refundRepository.save(refund);
    }

    public Refund getRefund(String refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));
    }

    public List<PaymentAttempt> getAttempts(String intentId) {
        return attemptRepository.findByPaymentIntentIdOrderByCreatedAtAsc(intentId);
    }
}
