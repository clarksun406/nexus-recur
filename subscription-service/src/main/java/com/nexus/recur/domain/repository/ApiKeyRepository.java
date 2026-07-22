package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.ApiKey;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
    Optional<ApiKey> findByKeyId(String keyId);
    Page<ApiKey> findByUserId(String userId, Pageable pageable);
}
