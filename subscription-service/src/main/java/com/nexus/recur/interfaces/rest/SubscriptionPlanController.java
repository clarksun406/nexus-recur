package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.PlanService;
import com.nexus.recur.domain.model.PlanStatus;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanRequest;
import com.nexus.recur.interfaces.rest.dto.PlanDtos.PlanResponse;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/plans")
public class SubscriptionPlanController {
    private final PlanService planService;

    public SubscriptionPlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @CheckPermission("plan:create")
    public ApiResponse<PlanResponse> create(@Valid @RequestBody PlanRequest request) {
        return ApiResponse.ok(planService.create(request));
    }

    @GetMapping
    @CheckPermission("plan:read")
    public ApiResponse<PageResult<PlanResponse>> list(@RequestParam(required = false) PlanStatus status,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(planService.list(status, page, limit));
    }

    @GetMapping("/{planId}")
    @CheckPermission("plan:read")
    public ApiResponse<PlanResponse> get(@PathVariable String planId) {
        return ApiResponse.ok(planService.get(planId));
    }

    @PutMapping("/{planId}")
    @CheckPermission("plan:update")
    public ApiResponse<PlanResponse> update(@PathVariable String planId, @Valid @RequestBody PlanRequest request) {
        return ApiResponse.ok(planService.update(planId, request));
    }

    @PostMapping("/{planId}/archive")
    @CheckPermission("plan:archive")
    public ApiResponse<PlanResponse> archive(@PathVariable String planId) {
        return ApiResponse.ok(planService.archive(planId));
    }
}
