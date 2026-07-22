package com.nexus.gateway.engine;

import com.nexus.gateway.config.GatewayProperties;
import com.nexus.gateway.provider.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RoutingEngine {

    private final GatewayProperties properties;
    private final CircuitBreaker circuitBreaker;

    public RoutingEngine(GatewayProperties properties, CircuitBreaker circuitBreaker) {
        this.properties = properties;
        this.circuitBreaker = circuitBreaker;
    }

    public List<PaymentProvider> resolve(String paymentIntentId) {
        List<GatewayProperties.ConnectorConfig> enabled = properties.connectors().stream()
                .filter(GatewayProperties.ConnectorConfig::enabled)
                .toList();

        List<PaymentProvider> candidates = new ArrayList<>();
        int seed = Math.abs(paymentIntentId.hashCode());
        int totalWeight = enabled.stream().mapToInt(GatewayProperties.ConnectorConfig::weight).sum();

        if (totalWeight == 0) {
            return candidates;
        }

        int pointer = seed % totalWeight;
        int cumulative = 0;
        GatewayProperties.ConnectorConfig primary = null;
        for (GatewayProperties.ConnectorConfig connector : enabled) {
            cumulative += connector.weight();
            if (pointer < cumulative) {
                primary = connector;
                break;
            }
        }
        if (primary == null) {
            primary = enabled.get(enabled.size() - 1);
        }

        PaymentProvider primaryProvider = PaymentProvider.valueOf(primary.provider());
        if (!circuitBreaker.isOpen(primaryProvider)) {
            candidates.add(primaryProvider);
        }

        for (GatewayProperties.ConnectorConfig connector : enabled) {
            PaymentProvider p = PaymentProvider.valueOf(connector.provider());
            if (p != primaryProvider && !circuitBreaker.isOpen(p)) {
                candidates.add(p);
            }
        }

        if (candidates.isEmpty()) {
            candidates.add(primaryProvider);
        }

        return candidates;
    }
}
