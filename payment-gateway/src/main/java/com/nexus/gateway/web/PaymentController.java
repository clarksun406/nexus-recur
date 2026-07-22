package com.nexus.gateway.web;

import com.nexus.gateway.domain.PaymentAttempt;
import com.nexus.gateway.domain.PaymentIntent;
import com.nexus.gateway.domain.Refund;
import com.nexus.gateway.engine.PaymentOrchestrator;
import com.nexus.gateway.web.dto.PaymentDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentOrchestrator orchestrator;

    public PaymentController(PaymentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/intents")
    public ResponseEntity<IntentResponse> createIntent(@RequestBody CreateIntentRequest request) {
        PaymentIntent intent = orchestrator.createIntent(
                request.merchantId(), request.amountCents(), request.currency(),
                request.description(), request.metadata(), request.paymentMethodId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toIntentResponse(intent));
    }

    @PostMapping("/intents/{id}/confirm")
    public ResponseEntity<ConfirmResponse> confirmIntent(@PathVariable String id) {
        PaymentIntent intent = orchestrator.confirm(id);
        List<PaymentAttempt> attempts = orchestrator.getAttempts(id);
        return ResponseEntity.ok(toConfirmResponse(intent, attempts));
    }

    @GetMapping("/intents/{id}")
    public ResponseEntity<IntentResponse> getIntent(@PathVariable String id) {
        PaymentIntent intent = orchestrator.getIntent(id);
        return ResponseEntity.ok(toIntentResponse(intent));
    }

    @PostMapping("/intents/{id}/cancel")
    public ResponseEntity<IntentResponse> cancelIntent(@PathVariable String id) {
        PaymentIntent intent = orchestrator.cancelIntent(id);
        return ResponseEntity.ok(toIntentResponse(intent));
    }

    @PostMapping("/refunds")
    public ResponseEntity<RefundResponse> createRefund(@RequestBody CreateRefundRequest request) {
        Refund refund = orchestrator.createRefund(
                request.paymentIntentId(), request.amountCents(), request.currency(), request.reason()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toRefundResponse(refund));
    }

    @GetMapping("/refunds/{id}")
    public ResponseEntity<RefundResponse> getRefund(@PathVariable String id) {
        Refund refund = orchestrator.getRefund(id);
        return ResponseEntity.ok(toRefundResponse(refund));
    }

    private IntentResponse toIntentResponse(PaymentIntent intent) {
        return new IntentResponse(
                intent.getId(), intent.getMerchantId(), intent.getAmountCents(),
                intent.getCurrency(), intent.getStatus(), intent.getProviderTransactionId(),
                intent.getDeclineCode(), intent.getAttemptCount(), intent.getMetadata(),
                intent.getCreatedAt()
        );
    }

    private ConfirmResponse toConfirmResponse(PaymentIntent intent, List<PaymentAttempt> attempts) {
        List<AttemptResponse> attemptResponses = attempts.stream()
                .map(a -> new AttemptResponse(
                        a.getProvider().name(), a.getStatus(), a.getDeclineCode(),
                        a.getRetryable(), a.getCreatedAt()
                ))
                .toList();
        return new ConfirmResponse(
                intent.getId(), intent.getStatus(), intent.getProviderTransactionId(),
                intent.getDeclineCode(), intent.getRetryable(), attemptResponses
        );
    }

    private RefundResponse toRefundResponse(Refund refund) {
        return new RefundResponse(
                refund.getId(), refund.getPaymentIntentId(), refund.getAmountCents(),
                refund.getCurrency(), refund.getStatus(), refund.getProviderRefundId(),
                refund.getCreatedAt()
        );
    }
}
