package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.EntityStatus;
import com.nexus.recur.domain.model.LegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalEntityRepository extends JpaRepository<LegalEntity, String> {
    List<LegalEntity> findByCountry(String country);
    List<LegalEntity> findByStatus(EntityStatus status);
}
