package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.AuditService;
import com.nexus.recur.domain.model.AuditLog;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.PageResult;
import com.nexusflow.permission.client.CheckPermission;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @CheckPermission("audit:read")
    public ApiResponse<PageResult<AuditLog>> query(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(auditService.query(actorId, resourceType, resourceId, action, page, limit));
    }
}
