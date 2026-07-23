package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, String> {
    List<RoutingRule> findByMerchantIdAndActiveTrueOrderByPriorityAsc(String merchantId);
    List<RoutingRule> findByMerchantId(String merchantId);
}
