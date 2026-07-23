package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.Settlement;
import com.nexus.recur.domain.model.SettlementStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, String> {
    Page<Settlement> findByMerchantId(String merchantId, Pageable pageable);
    Page<Settlement> findByMerchantIdAndStatus(String merchantId, SettlementStatus status, Pageable pageable);
    List<Settlement> findByMerchantIdAndCreatedAtBetween(String merchantId, OffsetDateTime start, OffsetDateTime end);
    List<Settlement> findByMerchantIdAndStatusAndCreatedAtBetween(String merchantId, SettlementStatus status, OffsetDateTime start, OffsetDateTime end);
}
