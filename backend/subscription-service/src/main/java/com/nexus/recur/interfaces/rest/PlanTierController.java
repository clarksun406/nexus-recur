package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.PlanTier;
import com.nexus.recur.domain.repository.PlanTierRepository;
import com.nexus.recur.domain.repository.SubscriptionPlanRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PlanTierDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/plans/{planId}/tiers")
public class PlanTierController {

    private final PlanTierRepository planTierRepository;
    private final SubscriptionPlanRepository planRepository;
    private final IdGenerator idGenerator;

    public PlanTierController(PlanTierRepository planTierRepository,
                               SubscriptionPlanRepository planRepository,
                               IdGenerator idGenerator) {
        this.planTierRepository = planTierRepository;
        this.planRepository = planRepository;
        this.idGenerator = idGenerator;
    }

    @PostMapping
    @CheckPermission("plan:update")
    public ApiResponse<TierResponse> create(@PathVariable String planId, @Valid @RequestBody CreateTierRequest request) {
        planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("PLAN_NOT_FOUND", "Plan not found"));

        PlanTier tier = new PlanTier();
        tier.setId(idGenerator.next("tier"));
        tier.setPlanId(planId);
        tier.setTierStart(request.tierStart());
        tier.setTierEnd(request.tierEnd());
        tier.setUnitAmountCents(request.unitAmountCents());
        tier.setFlatAmountCents(request.flatAmountCents());
        planTierRepository.save(tier);

        return ApiResponse.ok(toResponse(tier));
    }

    @GetMapping
    @CheckPermission("plan:read")
    public ApiResponse<List<TierResponse>> list(@PathVariable String planId) {
        List<TierResponse> tiers = planTierRepository.findByPlanIdOrderByTierStartAsc(planId)
                .stream().map(this::toResponse).toList();
        return ApiResponse.ok(tiers);
    }

    @DeleteMapping("/{tierId}")
    @CheckPermission("plan:update")
    public ApiResponse<Void> delete(@PathVariable String planId, @PathVariable String tierId) {
        PlanTier tier = planTierRepository.findById(tierId)
                .orElseThrow(() -> new BusinessException("TIER_NOT_FOUND", "Tier not found"));
        if (!tier.getPlanId().equals(planId)) {
            throw new BusinessException("TIER_PLAN_MISMATCH", "Tier does not belong to this plan");
        }
        planTierRepository.delete(tier);
        return ApiResponse.ok(null);
    }

    private TierResponse toResponse(PlanTier t) {
        return new TierResponse(t.getId(), t.getPlanId(), t.getTierStart(), t.getTierEnd(),
                t.getUnitAmountCents(), t.getFlatAmountCents());
    }
}
