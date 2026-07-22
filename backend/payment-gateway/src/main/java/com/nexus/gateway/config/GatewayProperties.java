package com.nexus.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(
        String merchantWebhookUrl,
        int maxAttempts,
        List<ConnectorConfig> connectors,
        CircuitBreakerConfig circuitBreaker
) {
    public record ConnectorConfig(String provider, int weight, boolean enabled) {}

    public record CircuitBreakerConfig(int failureThreshold, int openDurationSeconds) {}
}
