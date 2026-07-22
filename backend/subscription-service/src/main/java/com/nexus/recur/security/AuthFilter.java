package com.nexus.recur.security;

import com.nexus.recur.application.service.ApiKeyService;
import com.nexus.recur.domain.model.ApiKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    private final ApiKeyService apiKeyService;

    public AuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (shouldSkip(path, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            sendError(response, HttpStatus.UNAUTHORIZED, "MISSING_AUTH", "Authorization header required");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        ApiKey key = apiKeyService.validate(token);
        if (key == null) {
            sendError(response, HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", "invalid or revoked api key");
            return;
        }

        request.setAttribute("userId", key.getUserId());
        request.setAttribute("merchantId", key.getMerchantId());
        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(String path, String method) {
        if (path.startsWith("/v1/webhooks")) return true;
        if ("POST".equals(method) && path.equals("/v1/api-keys")) return true;
        return false;
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(CONTENT_TYPE);
        response.getWriter().write("{\"success\":false,\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"data\":null}");
    }
}
