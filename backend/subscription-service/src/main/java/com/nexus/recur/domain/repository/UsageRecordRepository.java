package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.UsageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, String> {
    Page<UsageRecord> findBySubscriptionId(String subscriptionId, Pageable pageable);
    boolean existsByIdempotencyKey(String idempotencyKey);
    List<UsageRecord> findBySubscriptionIdAndRecordedAtBetween(String subscriptionId, OffsetDateTime start, OffsetDateTime end);
}
