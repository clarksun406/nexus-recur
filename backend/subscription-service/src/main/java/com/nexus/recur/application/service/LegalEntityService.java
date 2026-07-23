package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.EntityStatus;
import com.nexus.recur.domain.model.LegalEntity;
import com.nexus.recur.domain.model.Merchant;
import com.nexus.recur.domain.repository.LegalEntityRepository;
import com.nexus.recur.domain.repository.MerchantRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.LegalEntityDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LegalEntityService {

    private final LegalEntityRepository legalEntityRepository;
    private final MerchantRepository merchantRepository;
    private final IdGenerator idGenerator;

    public LegalEntityService(LegalEntityRepository legalEntityRepository,
                              MerchantRepository merchantRepository,
                              IdGenerator idGenerator) {
        this.legalEntityRepository = legalEntityRepository;
        this.merchantRepository = merchantRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public LegalEntityResponse create(CreateEntityRequest request) {
        LegalEntity entity = new LegalEntity();
        entity.setId(idGenerator.next("ent"));
        entity.setName(request.name());
        entity.setRegistrationNumber(request.registrationNumber());
        entity.setCountry(request.country() != null ? request.country().toUpperCase() : "US");
        entity.setTaxId(request.taxId());
        entity.setAddressJson(request.addressJson());
        entity.setBankAccountJson(request.bankAccountJson());
        entity.setPrimaryContact(request.primaryContact());
        entity.setPrimaryEmail(request.primaryEmail());
        entity.setBaseCurrency(request.baseCurrency() != null ? request.baseCurrency().toUpperCase() : "USD");
        legalEntityRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<LegalEntityResponse> list() {
        return legalEntityRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LegalEntityResponse get(String entityId) {
        return toResponse(getEntity(entityId));
    }

    @Transactional
    public LegalEntityResponse update(String entityId, UpdateEntityRequest request) {
        LegalEntity entity = getEntity(entityId);
        if (request.name() != null) entity.setName(request.name());
        if (request.registrationNumber() != null) entity.setRegistrationNumber(request.registrationNumber());
        if (request.country() != null) entity.setCountry(request.country().toUpperCase());
        if (request.taxId() != null) entity.setTaxId(request.taxId());
        if (request.addressJson() != null) entity.setAddressJson(request.addressJson());
        if (request.bankAccountJson() != null) entity.setBankAccountJson(request.bankAccountJson());
        if (request.primaryContact() != null) entity.setPrimaryContact(request.primaryContact());
        if (request.primaryEmail() != null) entity.setPrimaryEmail(request.primaryEmail());
        if (request.baseCurrency() != null) entity.setBaseCurrency(request.baseCurrency().toUpperCase());
        if (request.status() != null) entity.setStatus(request.status());
        legalEntityRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public void assignMerchant(String entityId, String merchantId) {
        LegalEntity entity = getEntity(entityId);
        if (entity.getStatus() != EntityStatus.active) {
            throw new BusinessException("ENTITY_NOT_ACTIVE", "cannot assign merchant to non-active entity");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new BusinessException("MERCHANT_NOT_FOUND", "merchant not found: " + merchantId));
        merchant.setLegalEntityId(entityId);
        merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public List<LegalEntityResponse> listByCountry(String country) {
        return legalEntityRepository.findByCountry(country.toUpperCase()).stream().map(this::toResponse).toList();
    }

    private LegalEntity getEntity(String entityId) {
        return legalEntityRepository.findById(entityId)
                .orElseThrow(() -> new BusinessException("ENTITY_NOT_FOUND", "legal entity not found: " + entityId));
    }

    private LegalEntityResponse toResponse(LegalEntity e) {
        return new LegalEntityResponse(e.getId(), e.getName(), e.getRegistrationNumber(),
                e.getCountry(), e.getTaxId(), e.getAddressJson(), e.getBankAccountJson(),
                e.getStatus(), e.getPrimaryContact(), e.getPrimaryEmail(),
                e.getBaseCurrency(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
