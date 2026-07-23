package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.ReconciliationService;
import com.nexus.recur.application.service.ReconciliationService.ReconciliationReport;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexusflow.permission.client.CheckPermission;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/report")
    @CheckPermission("reconciliation:read")
    public ApiResponse<ReconciliationReport> report(
            @RequestParam(required = false) String merchantId,
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.ok(reconciliationService.monthlyReport(merchantId, year, month));
    }

    @GetMapping("/export")
    @CheckPermission("reconciliation:export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String merchantId,
            @RequestParam int year,
            @RequestParam int month) {
        String csv = reconciliationService.exportCsv(merchantId, year, month);
        String filename = String.format("reconciliation_%d_%02d.csv", year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
