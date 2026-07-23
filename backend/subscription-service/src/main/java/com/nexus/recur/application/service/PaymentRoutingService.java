package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.RoutingRule;
import com.nexus.recur.domain.model.RoutingStrategy;
import com.nexus.recur.domain.repository.RoutingRuleRepository;
import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.RoutingDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PaymentRoutingService {

    private final RoutingRuleRepository routingRuleRepository;
    private final IdGenerator idGenerator;

    public PaymentRoutingService(RoutingRuleRepository routingRuleRepository, IdGenerator idGenerator) {
        this.routingRuleRepository = routingRuleRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public RoutingRuleResponse create(String merchantId, CreateRoutingRuleRequest request) {
        RoutingRule rule = new RoutingRule();
        rule.setId(idGenerator.next("rtr"));
        rule.setMerchantId(merchantId);
        rule.setProviderName(request.providerName());
        rule.setPriority(request.priority());
        rule.setCurrency(request.currency() != null ? request.currency().toUpperCase() : null);
        rule.setMinAmountCents(request.minAmountCents());
        rule.setMaxAmountCents(request.maxAmountCents());
        rule.setRegion(request.region());
        rule.setWeight(request.weight() != null ? request.weight() : 1);
        rule.setStrategy(request.strategy() != null ? request.strategy() : RoutingStrategy.priority);
        rule.setSuccessRate(request.successRate() != null ? request.successRate() : 0.95);
        rule.setCostPercentage(request.costPercentage() != null ? request.costPercentage() : 2.9);
        routingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleResponse> list(String merchantId) {
        return routingRuleRepository.findByMerchantId(merchantId).stream()
                .sorted(Comparator.comparingInt(RoutingRule::getPriority))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoutingRuleResponse update(String ruleId, UpdateRoutingRuleRequest request) {
        RoutingRule rule = getEntity(ruleId);
        if (request.providerName() != null) rule.setProviderName(request.providerName());
        if (request.priority() != null) rule.setPriority(request.priority());
        if (request.currency() != null) rule.setCurrency(request.currency().toUpperCase());
        if (request.minAmountCents() != null) rule.setMinAmountCents(request.minAmountCents());
        if (request.maxAmountCents() != null) rule.setMaxAmountCents(request.maxAmountCents());
        if (request.region() != null) rule.setRegion(request.region());
        if (request.weight() != null) rule.setWeight(request.weight());
        if (request.active() != null) rule.setActive(request.active());
        if (request.strategy() != null) rule.setStrategy(request.strategy());
        if (request.successRate() != null) rule.setSuccessRate(request.successRate());
        if (request.costPercentage() != null) rule.setCostPercentage(request.costPercentage());
        routingRuleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public void delete(String ruleId) {
        RoutingRule rule = getEntity(ruleId);
        routingRuleRepository.delete(rule);
    }

    public RoutingDecision resolve(String merchantId, String currency, long amountCents, String region) {
        List<RoutingRule> candidates = routingRuleRepository
                .findByMerchantIdAndActiveTrueOrderByPriorityAsc(merchantId)
                .stream()
                .filter(r -> matches(r, currency, amountCents, region))
                .toList();

        if (candidates.isEmpty()) {
            return new RoutingDecision("default", "no matching rule, fallback to default provider", null);
        }

        RoutingStrategy strategy = candidates.get(0).getStrategy();
        RoutingRule selected = switch (strategy) {
            case priority -> candidates.get(0);
            case cost_optimized -> candidates.stream()
                    .min(Comparator.comparingDouble(RoutingRule::getCostPercentage))
                    .orElse(candidates.get(0));
            case success_rate -> candidates.stream()
                    .max(Comparator.comparingDouble(RoutingRule::getSuccessRate))
                    .orElse(candidates.get(0));
            case weighted -> selectWeighted(candidates);
        };

        String reason = switch (strategy) {
            case priority -> "highest priority (priority=" + selected.getPriority() + ")";
            case cost_optimized -> "lowest cost (" + selected.getCostPercentage() + "%)";
            case success_rate -> "highest success rate (" + selected.getSuccessRate() + ")";
            case weighted -> "weighted selection (weight=" + selected.getWeight() + ")";
        };

        return new RoutingDecision(selected.getProviderName(), reason, selected.getId());
    }

    private RoutingRule selectWeighted(List<RoutingRule> candidates) {
        int totalWeight = candidates.stream().mapToInt(RoutingRule::getWeight).sum();
        int target = (int) (System.nanoTime() % totalWeight);
        int cumulative = 0;
        for (RoutingRule rule : candidates) {
            cumulative += rule.getWeight();
            if (target < cumulative) return rule;
        }
        return candidates.get(candidates.size() - 1);
    }

    private boolean matches(RoutingRule rule, String currency, long amountCents, String region) {
        if (rule.getCurrency() != null && !rule.getCurrency().equalsIgnoreCase(currency)) return false;
        if (rule.getMinAmountCents() != null && amountCents < rule.getMinAmountCents()) return false;
        if (rule.getMaxAmountCents() != null && amountCents > rule.getMaxAmountCents()) return false;
        if (rule.getRegion() != null && !rule.getRegion().equalsIgnoreCase(region)) return false;
        return true;
    }

    private RoutingRule getEntity(String ruleId) {
        return routingRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException("ROUTING_RULE_NOT_FOUND", "routing rule not found: " + ruleId));
    }

    private RoutingRuleResponse toResponse(RoutingRule r) {
        return new RoutingRuleResponse(r.getId(), r.getMerchantId(), r.getProviderName(),
                r.getPriority(), r.getCurrency(), r.getMinAmountCents(), r.getMaxAmountCents(),
                r.getRegion(), r.getWeight(), r.isActive(), r.getStrategy(),
                r.getSuccessRate(), r.getCostPercentage(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
