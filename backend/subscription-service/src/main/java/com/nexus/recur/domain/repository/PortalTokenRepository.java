package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.PortalToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortalTokenRepository extends JpaRepository<PortalToken, String> {
    Optional<PortalToken> findByToken(String token);
}
