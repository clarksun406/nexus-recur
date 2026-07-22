package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.Subscription;
import com.nexus.recur.domain.model.SubscriptionStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Page<Subscription> findByUserId(String userId, Pageable pageable);
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);
    Page<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status, Pageable pageable);
    Optional<Subscription> findByExternalSubId(String externalSubId);
    List<Subscription> findByStatusAndCurrentPeriodEndBefore(SubscriptionStatus status, OffsetDateTime before);
    List<Subscription> findByStatusAndTrialEndAtBefore(SubscriptionStatus status, OffsetDateTime before);
    List<Subscription> findByStatusAndNextRetryAtBefore(SubscriptionStatus status, OffsetDateTime before);
    long countByStatus(SubscriptionStatus status);
}
