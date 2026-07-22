package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.WebhookEndpoint;
import com.nexus.recur.domain.model.WebhookEndpointStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, String> {
    List<WebhookEndpoint> findByMerchantId(String merchantId);
    List<WebhookEndpoint> findByStatus(WebhookEndpointStatus status);
}
