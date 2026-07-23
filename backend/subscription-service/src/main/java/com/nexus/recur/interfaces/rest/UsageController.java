package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.UsageRecordService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.UsageDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/usage")
public class UsageController {

    private final UsageRecordService usageRecordService;

    public UsageController(UsageRecordService usageRecordService) {
        this.usageRecordService = usageRecordService;
    }

    @PostMapping
    @CheckPermission("usage:report")
    public ApiResponse<UsageRecordResponse> report(@Valid @RequestBody ReportUsageRequest request) {
        return ApiResponse.ok(usageRecordService.report(request));
    }

    @GetMapping
    @CheckPermission("usage:read")
    public ApiResponse<Page<UsageRecordResponse>> list(
            @RequestParam String subscriptionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(usageRecordService.list(subscriptionId, page, limit));
    }
}
