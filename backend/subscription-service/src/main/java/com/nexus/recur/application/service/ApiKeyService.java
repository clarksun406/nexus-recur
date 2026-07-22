package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.ApiKey;
import com.nexus.recur.domain.model.ApiKeyScope;
import com.nexus.recur.domain.model.ApiKeyStatus;
import com.nexus.recur.domain.repository.ApiKeyRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyService {
    private static final String KEY_PREFIX = "sk_live_";
    private static final int RANDOM_LENGTH = 48;
    private static final int KEY_ID_LENGTH = 16;
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

    private final ApiKeyRepository repository;
    private final IdGenerator idGenerator;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public ApiKeyService(ApiKeyRepository repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public CreatedKey create(String userId, String merchantId, ApiKeyScope scope) {
        String randomPart = generateRandom(RANDOM_LENGTH);
        String fullKey = KEY_PREFIX + randomPart;
        String keyId = randomPart.substring(0, KEY_ID_LENGTH);
        String keyPrefix = fullKey.substring(0, Math.min(fullKey.length(), 16));

        ApiKey entity = new ApiKey();
        entity.setId(idGenerator.next("apk"));
        entity.setKeyId(keyId);
        entity.setKeyHash(encoder.encode(fullKey));
        entity.setKeyPrefix(keyPrefix);
        entity.setScope(scope == null ? ApiKeyScope.full_access : scope);
        entity.setUserId(userId);
        entity.setMerchantId(merchantId);
        entity.setStatus(ApiKeyStatus.active);
        entity.setCreatedAt(OffsetDateTime.now());
        repository.save(entity);

        return new CreatedKey(entity.getId(), fullKey, keyPrefix);
    }

    @Transactional
    public void revoke(String keyEntityId) {
        ApiKey key = repository.findById(keyEntityId)
                .orElseThrow(() -> new BusinessException("API_KEY_NOT_FOUND", "api key not found: " + keyEntityId));
        key.setStatus(ApiKeyStatus.revoked);
        repository.save(key);
    }

    @Transactional(readOnly = true)
    public Page<ApiKey> list(String userId, int page, int limit) {
        return repository.findByUserId(userId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)));
    }

    @Transactional
    public ApiKey validate(String fullKey) {
        if (fullKey == null || !fullKey.startsWith(KEY_PREFIX)) {
            return null;
        }
        String randomPart = fullKey.substring(KEY_PREFIX.length());
        if (randomPart.length() < KEY_ID_LENGTH) {
            return null;
        }
        String keyId = randomPart.substring(0, KEY_ID_LENGTH);
        ApiKey key = repository.findByKeyId(keyId).orElse(null);
        if (key == null || key.getStatus() != ApiKeyStatus.active) {
            return null;
        }
        if (!encoder.matches(fullKey, key.getKeyHash())) {
            return null;
        }
        key.setLastUsedAt(OffsetDateTime.now());
        repository.save(key);
        return key;
    }

    private String generateRandom(int length) {
        char[] value = new char[length];
        for (int i = 0; i < length; i++) {
            value[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(value);
    }

    public record CreatedKey(String id, String key, String prefix) {}
}
