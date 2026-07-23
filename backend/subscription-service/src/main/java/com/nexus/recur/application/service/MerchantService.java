package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.KycStatus;
import com.nexus.recur.domain.model.Merchant;
import com.nexus.recur.domain.repository.MerchantRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MerchantService {

    private static final List<String> AUTO_CURRENCIES = List.of("USD", "EUR", "GBP");

    private final MerchantRepository merchantRepository;
    private final WalletService walletService;
    private final IdGenerator idGenerator;

    public MerchantService(MerchantRepository merchantRepository, WalletService walletService, IdGenerator idGenerator) {
        this.merchantRepository = merchantRepository;
        this.walletService = walletService;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Merchant register(String name, String email) {
        Merchant merchant = new Merchant();
        merchant.setId(idGenerator.next("mch"));
        merchant.setName(name);
        merchant.setEmail(email);
        merchant.setKycStatus(KycStatus.none);
        return merchantRepository.save(merchant);
    }

    @Transactional
    public Merchant submitKyc(String merchantId) {
        Merchant merchant = getEntity(merchantId);
        if (merchant.getKycStatus() != KycStatus.none && merchant.getKycStatus() != KycStatus.rejected) {
            throw new BusinessException("INVALID_KYC_STATE", "KYC already submitted or approved");
        }
        merchant.setKycStatus(KycStatus.submitted);
        merchant.setKycSubmittedAt(OffsetDateTime.now());
        return merchantRepository.save(merchant);
    }

    @Transactional
    public Merchant approveKyc(String merchantId) {
        Merchant merchant = getEntity(merchantId);
        if (merchant.getKycStatus() != KycStatus.submitted) {
            throw new BusinessException("INVALID_KYC_STATE", "KYC not in submitted state");
        }
        merchant.setKycStatus(KycStatus.approved);
        merchant.setKycApprovedAt(OffsetDateTime.now());
        merchantRepository.save(merchant);

        for (String currency : AUTO_CURRENCIES) {
            walletService.applyToWallet(merchantId, currency, java.math.BigDecimal.ZERO,
                    com.nexus.recur.domain.model.WalletTransactionType.adjustment,
                    "KYC auto-open " + currency + " wallet", "merchant", merchantId);
        }

        return merchant;
    }

    @Transactional
    public Merchant rejectKyc(String merchantId, String reason) {
        Merchant merchant = getEntity(merchantId);
        if (merchant.getKycStatus() != KycStatus.submitted) {
            throw new BusinessException("INVALID_KYC_STATE", "KYC not in submitted state");
        }
        merchant.setKycStatus(KycStatus.rejected);
        merchant.setKycRejectedReason(reason);
        return merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public Merchant get(String merchantId) {
        return getEntity(merchantId);
    }

    @Transactional(readOnly = true)
    public List<Merchant> list() {
        return merchantRepository.findAll();
    }

    public void requireKycApproved(String merchantId) {
        Merchant merchant = getEntity(merchantId);
        if (merchant.getKycStatus() != KycStatus.approved) {
            throw new BusinessException("KYC_NOT_APPROVED", "merchant KYC is not approved");
        }
    }

    private Merchant getEntity(String merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("MERCHANT_NOT_FOUND", "merchant not found: " + merchantId));
    }
}
