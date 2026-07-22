package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.SubscriptionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionEventRepository extends JpaRepository<SubscriptionEvent, String> {
    Page<SubscriptionEvent> findBySubscriptionId(String subscriptionId, Pageable pageable);
}
