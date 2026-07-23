package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.WebhookDelivery;
import com.nexus.recur.domain.model.WebhookDeliveryStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, String> {
    List<WebhookDelivery> findByStatusAndNextRetryAtBefore(WebhookDeliveryStatus status, OffsetDateTime before);
    Page<WebhookDelivery> findByEndpointId(String endpointId, Pageable pageable);
}
