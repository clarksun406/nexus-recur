package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.LicenseKey;
import com.nexus.recur.domain.model.LicenseStatus;
import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.repository.LicenseKeyRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
public class LicenseKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final LicenseKeyRepository licenseKeyRepository;
    private final PlanService planService;
    private final IdGenerator idGenerator;

    public LicenseKeyService(LicenseKeyRepository licenseKeyRepository,
                              PlanService planService,
                              IdGenerator idGenerator) {
        this.licenseKeyRepository = licenseKeyRepository;
        this.planService = planService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public LicenseKey generate(String merchantId, String planId, String subscriptionId,
                                int maxActivations, Integer expiryDays) {
        SubscriptionPlan plan = planService.getEntity(planId);
        if (!plan.isLicenseEnabled()) {
            throw new BusinessException("LICENSE_DISABLED", "Plan does not have licensing enabled");
        }

        LicenseKey license = new LicenseKey();
        license.setId(idGenerator.next("lic"));
        license.setLicenseKey(formatKey(generateRawKey()));
        license.setMerchantId(merchantId);
        license.setPlanId(planId);
        license.setSubscriptionId(subscriptionId);
        license.setMaxActivations(maxActivations > 0 ? maxActivations : plan.getLicenseInstanceLimit());
        if (expiryDays != null && expiryDays > 0) {
            license.setExpiresAt(OffsetDateTime.now().plusDays(expiryDays));
        }
        return licenseKeyRepository.save(license);
    }

    @Transactional
    public LicenseKey validate(String rawKey, String deviceFingerprint) {
        LicenseKey license = licenseKeyRepository.findByLicenseKey(rawKey)
                .orElseThrow(() -> new BusinessException("LICENSE_NOT_FOUND", "Invalid license key"));

        if (license.getStatus() != LicenseStatus.active) {
            throw new BusinessException("LICENSE_INVALID", "License is " + license.getStatus());
        }
        if (license.getExpiresAt() != null && license.getExpiresAt().isBefore(OffsetDateTime.now())) {
            license.setStatus(LicenseStatus.expired);
            licenseKeyRepository.save(license);
            throw new BusinessException("LICENSE_EXPIRED", "License has expired");
        }

        if (license.getDeviceFingerprint() == null) {
            license.setDeviceFingerprint(deviceFingerprint);
            license.setCurrentActivations(license.getCurrentActivations() + 1);
        } else if (!license.getDeviceFingerprint().equals(deviceFingerprint)) {
            if (license.getCurrentActivations() >= license.getMaxActivations()) {
                throw new BusinessException("ACTIVATION_LIMIT", "Maximum activations reached");
            }
            license.setCurrentActivations(license.getCurrentActivations() + 1);
        }

        license.setLastValidatedAt(OffsetDateTime.now());
        return licenseKeyRepository.save(license);
    }

    @Transactional
    public LicenseKey suspend(String licenseId) {
        LicenseKey license = getEntity(licenseId);
        license.setStatus(LicenseStatus.suspended);
        return licenseKeyRepository.save(license);
    }

    @Transactional
    public LicenseKey revoke(String licenseId) {
        LicenseKey license = getEntity(licenseId);
        license.setStatus(LicenseStatus.revoked);
        return licenseKeyRepository.save(license);
    }

    @Transactional
    public LicenseKey reactivate(String licenseId) {
        LicenseKey license = getEntity(licenseId);
        if (license.getStatus() == LicenseStatus.revoked) {
            throw new BusinessException("LICENSE_REVOKED", "Cannot reactivate a revoked license");
        }
        license.setStatus(LicenseStatus.active);
        return licenseKeyRepository.save(license);
    }

    @Transactional(readOnly = true)
    public LicenseKey get(String licenseId) {
        return getEntity(licenseId);
    }

    @Transactional(readOnly = true)
    public Page<LicenseKey> list(String merchantId, int page, int limit) {
        return licenseKeyRepository.findByMerchantId(merchantId,
                PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    private LicenseKey getEntity(String licenseId) {
        return licenseKeyRepository.findById(licenseId)
                .orElseThrow(() -> new BusinessException("LICENSE_NOT_FOUND", "License not found"));
    }

    private String generateRawKey() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String formatKey(String raw) {
        return raw.substring(0, 4) + "-" + raw.substring(4, 8) + "-" + raw.substring(8, 12) + "-" + raw.substring(12, 16);
    }
}
