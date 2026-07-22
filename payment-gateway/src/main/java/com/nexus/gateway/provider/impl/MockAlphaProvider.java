package com.nexus.gateway.provider.impl;

import com.nexus.gateway.provider.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockAlphaProvider implements PaymentProviderService {

    @Override
    public PaymentProvider brand() {
        return PaymentProvider.MOCK_ALPHA;
    }

    @Override
    public ChargeResult charge(ChargeRequest request) {
        simulateLatency(50);
        return ChargeResult.success("alpha_" + UUID.randomUUID());
    }

    @Override
    public RefundResult refund(RefundRequest request) {
        simulateLatency(50);
        return RefundResult.success("alpha_re_" + UUID.randomUUID());
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
