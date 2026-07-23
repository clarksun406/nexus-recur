package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.WebhookDelivery;
import com.nexus.recur.domain.model.WebhookEndpoint;
import com.nexus.recur.domain.model.WebhookEndpointStatus;
import com.nexus.recur.domain.repository.WebhookDeliveryRepository;
import com.nexus.recur.domain.repository.WebhookEndpointRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.WebhookEndpointDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/webhook-endpoints")
public class WebhookEndpointController {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final IdGenerator idGenerator;

    public WebhookEndpointController(WebhookEndpointRepository endpointRepository,
                                      WebhookDeliveryRepository deliveryRepository,
                                      IdGenerator idGenerator) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.idGenerator = idGenerator;
    }

    @PostMapping
    @CheckPermission("webhook:create")
    public ApiResponse<EndpointResponse> create(@Valid @RequestBody CreateEndpointRequest request) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String secret = "whsec_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setId(idGenerator.next("whe"));
        endpoint.setMerchantId(request.merchantId());
        endpoint.setUrl(request.url());
        endpoint.setSecret(secret);
        endpoint.setSubscribedEvents(request.events() != null ? String.join(",", request.events()) : null);
        endpointRepository.save(endpoint);

        return ApiResponse.ok(toResponse(endpoint));
    }

    @GetMapping
    @CheckPermission("webhook:read")
    public ApiResponse<List<EndpointResponse>> list(@RequestParam(required = false) String merchantId) {
        List<WebhookEndpoint> endpoints = merchantId != null
                ? endpointRepository.findByMerchantId(merchantId)
                : endpointRepository.findAll();
        return ApiResponse.ok(endpoints.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @CheckPermission("webhook:read")
    public ApiResponse<EndpointResponse> get(@PathVariable String id) {
        return ApiResponse.ok(toResponse(getEntity(id)));
    }

    @PutMapping("/{id}")
    @CheckPermission("webhook:update")
    public ApiResponse<EndpointResponse> update(@PathVariable String id, @RequestBody UpdateEndpointRequest request) {
        WebhookEndpoint endpoint = getEntity(id);
        if (request.url() != null) endpoint.setUrl(request.url());
        if (request.events() != null) endpoint.setSubscribedEvents(String.join(",", request.events()));
        if (request.status() != null) endpoint.setStatus(WebhookEndpointStatus.valueOf(request.status()));
        endpointRepository.save(endpoint);
        return ApiResponse.ok(toResponse(endpoint));
    }

    @DeleteMapping("/{id}")
    @CheckPermission("webhook:delete")
    public ApiResponse<Void> delete(@PathVariable String id) {
        WebhookEndpoint endpoint = getEntity(id);
        endpoint.setStatus(WebhookEndpointStatus.disabled);
        endpointRepository.save(endpoint);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/deliveries")
    @CheckPermission("webhook:read")
    public ApiResponse<PageResult<DeliveryResponse>> deliveries(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        getEntity(id);
        Page<WebhookDelivery> result = deliveryRepository.findByEndpointId(
                id, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "createdAt")));
        List<DeliveryResponse> items = result.getContent().stream().map(this::toDeliveryResponse).toList();
        return ApiResponse.ok(new PageResult<>(items, page, limit, result.getTotalElements()));
    }

    private WebhookEndpoint getEntity(String id) {
        return endpointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("ENDPOINT_NOT_FOUND", "Webhook endpoint not found"));
    }

    private EndpointResponse toResponse(WebhookEndpoint e) {
        List<String> events = (e.getSubscribedEvents() == null || e.getSubscribedEvents().isBlank())
                ? List.of()
                : Arrays.asList(e.getSubscribedEvents().split(","));
        return new EndpointResponse(e.getId(), e.getMerchantId(), e.getUrl(), e.getSecret(),
                events, e.getStatus().name(), e.getCreatedAt());
    }

    private DeliveryResponse toDeliveryResponse(WebhookDelivery d) {
        return new DeliveryResponse(d.getId(), d.getEndpointId(), d.getEventType(), d.getStatus().name(),
                d.getAttempts(), d.getMaxAttempts(), d.getResponseCode(), d.getResponseMessage(), d.getCreatedAt());
    }
}
