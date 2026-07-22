package com.nexus.recur.application.service;

import com.nexus.recur.application.service.ApiKeyService.CreatedKey;
import com.nexus.recur.domain.model.ApiKey;
import com.nexus.recur.domain.model.ApiKeyScope;
import com.nexus.recur.domain.model.ApiKeyStatus;
import com.nexus.recur.domain.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ApiKeyServiceTest {
    @Autowired
    private ApiKeyService apiKeyService;
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @BeforeEach
    void cleanUp() {
        apiKeyRepository.deleteAll();
    }

    @Test
    void createsKeyWithCorrectFormat() {
        CreatedKey created = apiKeyService.create("user_1", "merchant_1", ApiKeyScope.full_access);

        assertThat(created.key()).startsWith("sk_live_");
        assertThat(created.key()).hasSize(8 + 48);
        assertThat(created.prefix()).startsWith("sk_live_");
        assertThat(created.id()).startsWith("apk_");

        ApiKey saved = apiKeyRepository.findById(created.id()).orElseThrow();
        assertThat(saved.getKeyId()).hasSize(16);
        assertThat(saved.getKeyHash()).startsWith("$2a$");
        assertThat(saved.getScope()).isEqualTo(ApiKeyScope.full_access);
        assertThat(saved.getUserId()).isEqualTo("user_1");
        assertThat(saved.getStatus()).isEqualTo(ApiKeyStatus.active);
        assertThat(saved.getLastUsedAt()).isNull();
    }

    @Test
    void validatesCorrectKey() {
        CreatedKey created = apiKeyService.create("user_2", null, ApiKeyScope.read_only);

        ApiKey validated = apiKeyService.validate(created.key());

        assertThat(validated).isNotNull();
        assertThat(validated.getUserId()).isEqualTo("user_2");
        assertThat(validated.getMerchantId()).isNull();
        assertThat(validated.getLastUsedAt()).isNotNull();
    }

    @Test
    void rejectsInvalidKey() {
        CreatedKey created = apiKeyService.create("user_3", null, null);

        assertThat(apiKeyService.validate("sk_live_" + "x".repeat(48))).isNull();
        assertThat(apiKeyService.validate("invalid")).isNull();
        assertThat(apiKeyService.validate(null)).isNull();
    }

    @Test
    void rejectsRevokedKey() {
        CreatedKey created = apiKeyService.create("user_4", null, null);
        apiKeyService.revoke(created.id());

        assertThat(apiKeyService.validate(created.key())).isNull();

        ApiKey saved = apiKeyRepository.findById(created.id()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ApiKeyStatus.revoked);
    }

    @Test
    void listsByUserId() {
        apiKeyService.create("user_5", null, null);
        apiKeyService.create("user_5", null, null);
        apiKeyService.create("user_6", null, null);

        var page = apiKeyService.list("user_5", 1, 10);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
