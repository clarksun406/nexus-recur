package com.nexus.gateway;

import com.nexus.gateway.engine.CircuitBreaker;
import com.nexus.gateway.engine.FailoverPolicy;
import com.nexus.gateway.provider.PaymentProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineUnitTest {

    @Test
    void failoverPolicy_hardDeclinesNotRetryable() {
        FailoverPolicy policy = new FailoverPolicy();
        assertFalse(policy.isRetryable("card_declined"));
        assertFalse(policy.isRetryable("insufficient_funds"));
        assertFalse(policy.isRetryable("expired_card"));
        assertFalse(policy.isRetryable("fraudulent"));
    }

    @Test
    void failoverPolicy_softDeclinesRetryable() {
        FailoverPolicy policy = new FailoverPolicy();
        assertTrue(policy.isRetryable("network_timeout"));
        assertTrue(policy.isRetryable("rate_limited"));
        assertTrue(policy.isRetryable(null));
    }

    @Test
    void circuitBreaker_opensAfterThreshold() {
        var props = new com.nexus.gateway.config.GatewayProperties(
                "http://localhost", 3,
                java.util.List.of(new com.nexus.gateway.config.GatewayProperties.ConnectorConfig("MOCK_ALPHA", 70, true)),
                new com.nexus.gateway.config.GatewayProperties.CircuitBreakerConfig(3, 30)
        );
        CircuitBreaker cb = new CircuitBreaker(props);

        assertFalse(cb.isOpen(PaymentProvider.MOCK_ALPHA));
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, true);
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, true);
        assertFalse(cb.isOpen(PaymentProvider.MOCK_ALPHA));
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, true);
        assertTrue(cb.isOpen(PaymentProvider.MOCK_ALPHA));
    }

    @Test
    void circuitBreaker_hardDeclineDoesNotOpen() {
        var props = new com.nexus.gateway.config.GatewayProperties(
                "http://localhost", 3,
                java.util.List.of(new com.nexus.gateway.config.GatewayProperties.ConnectorConfig("MOCK_ALPHA", 70, true)),
                new com.nexus.gateway.config.GatewayProperties.CircuitBreakerConfig(3, 30)
        );
        CircuitBreaker cb = new CircuitBreaker(props);

        cb.recordFailure(PaymentProvider.MOCK_ALPHA, false);
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, false);
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, false);
        assertFalse(cb.isOpen(PaymentProvider.MOCK_ALPHA));
    }

    @Test
    void circuitBreaker_successResets() {
        var props = new com.nexus.gateway.config.GatewayProperties(
                "http://localhost", 3,
                java.util.List.of(new com.nexus.gateway.config.GatewayProperties.ConnectorConfig("MOCK_ALPHA", 70, true)),
                new com.nexus.gateway.config.GatewayProperties.CircuitBreakerConfig(3, 30)
        );
        CircuitBreaker cb = new CircuitBreaker(props);

        cb.recordFailure(PaymentProvider.MOCK_ALPHA, true);
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, true);
        cb.recordSuccess(PaymentProvider.MOCK_ALPHA);
        cb.recordFailure(PaymentProvider.MOCK_ALPHA, true);
        assertFalse(cb.isOpen(PaymentProvider.MOCK_ALPHA));
    }
}
