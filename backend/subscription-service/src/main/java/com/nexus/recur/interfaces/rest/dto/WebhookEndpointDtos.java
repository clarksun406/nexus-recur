package com.nexus.recur.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;

public final class WebhookEndpointDtos {
    private WebhookEndpointDtos() {}

    public record CreateEndpointRequest(
            @NotBlank String url,
            String merchantId,
            List<String> events
    ) {}

    public record UpdateEndpointRequest(
            String url,
            List<String> events,
            String status
    ) {}

    public record EndpointResponse(
            String id,
            String merchantId,
            String url,
            String secret,
            List<String> events,
            String status,
            OffsetDateTime createdAt
    ) {}

    public record DeliveryResponse(
            String id,
            String endpointId,
            String eventType,
            String status,
            int attempts,
            int maxAttempts,
            Integer responseCode,
            String responseMessage,
            OffsetDateTime createdAt
    ) {}
}
