package com.nexus.gateway.provider.impl;

import com.nexus.gateway.provider.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockBetaProvider implements PaymentProviderService {

    @Override
    public PaymentProvider brand() {
        return PaymentProvider.MOCK_BETA;
    }

    @Override
    public ChargeResult charge(ChargeRequest request) {
        simulateLatency(100);
        int hash = Math.abs(request.idempotencyKey().hashCode());
        if (hash % 10 < 7) {
            return ChargeResult.success("beta_" + UUID.randomUUID());
        }
        boolean retryable = (hash / 10) % 2 == 0;
        String declineCode = retryable ? "network_timeout" : "card_declined";
        return ChargeResult.failure(declineCode, retryable);
    }

    @Override
    public RefundResult refund(RefundRequest request) {
        simulateLatency(100);
        int hash = Math.abs(request.providerTransactionId().hashCode());
        if (hash % 10 < 9) {
            return RefundResult.success("beta_re_" + UUID.randomUUID());
        }
        return RefundResult.failure();
    }

    @Override
    public boolean cancel(String providerTransactionId) {
        return true;
    }

    private void simulateLatency(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
