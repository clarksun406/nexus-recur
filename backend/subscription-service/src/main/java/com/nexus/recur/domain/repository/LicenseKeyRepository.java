package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.LicenseKey;
import com.nexus.recur.domain.model.LicenseStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseKeyRepository extends JpaRepository<LicenseKey, String> {
    Optional<LicenseKey> findByLicenseKey(String licenseKey);
    Page<LicenseKey> findByMerchantId(String merchantId, Pageable pageable);
    List<LicenseKey> findBySubscriptionId(String subscriptionId);
    Page<LicenseKey> findByStatus(LicenseStatus status, Pageable pageable);
}
