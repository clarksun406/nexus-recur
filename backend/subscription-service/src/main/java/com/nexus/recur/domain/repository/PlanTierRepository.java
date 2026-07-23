package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.PlanTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanTierRepository extends JpaRepository<PlanTier, String> {
    List<PlanTier> findByPlanIdOrderByTierStartAsc(String planId);
}
