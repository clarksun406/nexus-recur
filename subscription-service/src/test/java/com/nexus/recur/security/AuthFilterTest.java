package com.nexus.recur.security;

import com.nexus.recur.application.service.ApiKeyService;
import com.nexus.recur.domain.model.ApiKey;
import com.nexus.recur.domain.model.ApiKeyScope;
import com.nexus.recur.domain.model.ApiKeyStatus;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFilterTest {
    private final ApiKeyService apiKeyService = mock(ApiKeyService.class);
    private final AuthFilter filter = new AuthFilter(apiKeyService);

    @Test
    void skipsWebhookPaths() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/webhooks/subscription");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(apiKeyService, never()).validate(any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void skipsApiKeyCreation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/api-keys");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(apiKeyService, never()).validate(any());
    }

    @Test
    void allowsValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/plans");
        request.addHeader("Authorization", "Bearer sk_live_validkey1234567890");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        ApiKey key = new ApiKey();
        key.setId("apk_1");
        key.setUserId("user_1");
        key.setMerchantId("merchant_1");
        when(apiKeyService.validate("sk_live_validkey1234567890")).thenReturn(key);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(request.getAttribute("userId")).isEqualTo("user_1");
        assertThat(request.getAttribute("merchantId")).isEqualTo("merchant_1");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsMissingAuthHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/plans");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void rejectsInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/plans");
        request.addHeader("Authorization", "Bearer invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(apiKeyService.validate("invalid")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
