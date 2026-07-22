package com.nexus.recur.security;

import com.nexus.recur.application.service.ApiKeyService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(name = "subscription.auth.enabled", havingValue = "true", matchIfMissing = false)
public class AuthFilterConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration(ApiKeyService apiKeyService) {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter(apiKeyService));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
