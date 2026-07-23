package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.RetryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetryLogRepository extends JpaRepository<RetryLog, String> {
    List<RetryLog> findBySubscriptionIdOrderByAttemptNumberDesc(String subscriptionId);
    List<RetryLog> findBySubscriptionId(String subscriptionId);
}
