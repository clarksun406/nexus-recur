package com.nexus.gateway.config;

import com.nexus.gateway.engine.FailoverPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {

    @Bean
    public FailoverPolicy failoverPolicy() {
        return new FailoverPolicy();
    }
}
