package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    Page<SubscriptionPlan> findByStatus(PlanStatus status, Pageable pageable);
}
