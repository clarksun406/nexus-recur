package com.nexus.recur.security;

import com.nexus.recur.application.service.AuditService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class AuditFilter implements Filter {

    private static final Set<String> AUDITED_METHODS = Set.of("POST", "PUT", "DELETE");
    private static final Set<String> AUDITED_PATHS = Set.of(
            "/v1/subscriptions", "/v1/plans", "/v1/wallets", "/v1/settlements",
            "/v1/api-keys", "/v1/payments"
    );

    private final AuditService auditService;

    public AuditFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        chain.doFilter(request, response);

        if (!AUDITED_METHODS.contains(req.getMethod())) return;
        String path = req.getRequestURI();
        boolean audited = AUDITED_PATHS.stream().anyMatch(path::startsWith);
        if (!audited) return;
        if (res.getStatus() >= 400) return;

        String actorId = (String) req.getAttribute("userId");
        String merchantId = (String) req.getAttribute("merchantId");
        String action = req.getMethod() + " " + path;
        String resourceType = extractResourceType(path);
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null) ip = req.getRemoteAddr();

        auditService.log(
                actorId != null ? actorId : merchantId,
                null,
                action,
                resourceType,
                extractResourceId(path, resourceType),
                ip,
                req.getHeader("User-Agent"),
                null
        );
    }

    private String extractResourceType(String path) {
        String[] parts = path.split("/");
        return parts.length >= 3 ? parts[2] : "unknown";
    }

    private String extractResourceId(String path, String resourceType) {
        String[] parts = path.split("/");
        if (parts.length >= 4 && !parts[3].equals("cancel") && !parts[3].equals("approve")) {
            return parts[3];
        }
        return null;
    }
}
