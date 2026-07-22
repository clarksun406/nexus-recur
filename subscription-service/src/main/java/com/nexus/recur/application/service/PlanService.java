package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.BillingType;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.TaxMode;
import com.nexus.recur.domain.repository.SubscriptionPlanRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.infrastructure.support.JsonService;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanRequest;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanService {
    private final SubscriptionPlanRepository planRepository;
    private final IdGenerator idGenerator;
    private final JsonService jsonService;

    public PlanService(SubscriptionPlanRepository planRepository, IdGenerator idGenerator, JsonService jsonService) {
        this.planRepository = planRepository;
        this.idGenerator = idGenerator;
        this.jsonService = jsonService;
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(idGenerator.next("plan"));
        apply(plan, request);
        if (request.status() == null) {
            plan.setStatus(PlanStatus.active);
        }
        return toResponse(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public PageResult<PlanResponse> list(PlanStatus status, int page, int limit) {
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1));
        Page<SubscriptionPlan> result = status == null ? planRepository.findAll(pageRequest) : planRepository.findByStatus(status, pageRequest);
        return new PageResult<>(result.map(this::toResponse).getContent(), page, limit, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PlanResponse get(String planId) { return toResponse(getEntity(planId)); }

    @Transactional
    public PlanResponse update(String planId, PlanRequest request) {
        SubscriptionPlan plan = getEntity(planId);
        apply(plan, request);
        return toResponse(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse archive(String planId) {
        SubscriptionPlan plan = getEntity(planId);
        plan.setStatus(PlanStatus.archived);
        return toResponse(planRepository.save(plan));
    }

    public SubscriptionPlan getEntity(String planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "subscription plan not found: " + planId));
    }

    private void apply(SubscriptionPlan plan, PlanRequest request) {
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setProductId(request.productId());
        plan.setBillingCycle(request.billingCycle());
        plan.setPriceCents(toCents(request.price()));
        plan.setCurrency(request.currency() == null || request.currency().isBlank() ? "USD" : request.currency().toUpperCase());
        plan.setTrialDays(request.trialDays() == null ? 0 : request.trialDays());
        plan.setFeaturesJson(jsonService.write(request.features()));
        if (request.status() != null) {
            plan.setStatus(request.status());
        }
        plan.setBillingType(request.billingType() == null ? BillingType.flat_rate : request.billingType());
        plan.setMeteredConfigJson(jsonService.write(request.meteredConfig()));
        plan.setTaxMode(request.taxMode() == null ? TaxMode.none : request.taxMode());
        plan.setTaxCategory(request.taxCategory());
        plan.setLicenseEnabled(request.licenseEnabled() != null && request.licenseEnabled());
        plan.setLicenseInstanceLimit(request.licenseInstanceLimit() == null ? 1 : request.licenseInstanceLimit());
    }

    private int toCents(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    public PlanResponse toResponse(SubscriptionPlan plan) {
        return new PlanResponse(plan.getId(), plan.getName(), plan.getDescription(), plan.getProductId(), plan.getBillingCycle(),
                BigDecimal.valueOf(plan.getPriceCents(), 2), plan.getCurrency(), plan.getTrialDays(),
                jsonService.read(plan.getFeaturesJson()), plan.getStatus(),
                plan.getBillingType(), jsonService.read(plan.getMeteredConfigJson()), plan.getTaxMode(), plan.getTaxCategory(),
                plan.isLicenseEnabled(), plan.getLicenseInstanceLimit(),
                plan.getCreatedAt(), plan.getUpdatedAt());
    }
}
