package com.nexus.gateway.engine;

import com.nexus.gateway.config.GatewayProperties;
import com.nexus.gateway.provider.PaymentProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CircuitBreaker {

    private final int failureThreshold;
    private final long openDurationMillis;
    private final ConcurrentHashMap<PaymentProvider, AtomicInteger> failureCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PaymentProvider, Instant> openedAt = new ConcurrentHashMap<>();

    public CircuitBreaker(GatewayProperties properties) {
        this.failureThreshold = properties.circuitBreaker().failureThreshold();
        this.openDurationMillis = properties.circuitBreaker().openDurationSeconds() * 1000L;
    }

    public boolean isOpen(PaymentProvider provider) {
        Instant opened = openedAt.get(provider);
        if (opened == null) {
            return false;
        }
        if (Instant.now().isAfter(opened.plusMillis(openDurationMillis))) {
            openedAt.remove(provider);
            failureCounts.remove(provider);
            return false;
        }
        return true;
    }

    public void recordSuccess(PaymentProvider provider) {
        failureCounts.remove(provider);
        openedAt.remove(provider);
    }

    public void recordFailure(PaymentProvider provider, boolean retryable) {
        if (!retryable) {
            failureCounts.remove(provider);
            return;
        }
        int count = failureCounts
                .computeIfAbsent(provider, k -> new AtomicInteger(0))
                .incrementAndGet();
        if (count >= failureThreshold) {
            openedAt.put(provider, Instant.now());
        }
    }
}
