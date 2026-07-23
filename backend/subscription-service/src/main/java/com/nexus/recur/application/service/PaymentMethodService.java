package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.PaymentMethod;
import com.nexus.recur.domain.model.PaymentMethodStatus;
import com.nexus.recur.domain.repository.PaymentMethodRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.PaymentMethodDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final CustomerService customerService;
    private final IdGenerator idGenerator;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
                                CustomerService customerService,
                                IdGenerator idGenerator) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.customerService = customerService;
        this.idGenerator = idGenerator;
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> list(String customerId) {
        return paymentMethodRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public PaymentMethodResponse create(String customerId, CreatePaymentMethodRequest request) {
        customerService.getEntity(customerId);
        PaymentMethod pm = new PaymentMethod();
        pm.setId(idGenerator.next("pm"));
        pm.setCustomerId(customerId);
        pm.setType(request.type());
        pm.setProvider(request.provider());
        pm.setCardBrand(request.cardBrand());
        pm.setCardLast4(request.cardLast4());
        pm.setExpMonth(request.expMonth());
        pm.setExpYear(request.expYear());
        pm.setBillingAddressJson(request.billingAddressJson());
        paymentMethodRepository.save(pm);
        return toResponse(pm);
    }

    @Transactional
    public PaymentMethodResponse setDefault(String pmId) {
        PaymentMethod pm = getEntity(pmId);
        List<PaymentMethod> existing = paymentMethodRepository.findByCustomerId(pm.getCustomerId());
        existing.forEach(e -> { e.setDefault(false); paymentMethodRepository.save(e); });
        pm.setDefault(true);
        paymentMethodRepository.save(pm);
        return toResponse(pm);
    }

    @Transactional
    public PaymentMethodResponse revoke(String pmId) {
        PaymentMethod pm = getEntity(pmId);
        pm.setStatus(PaymentMethodStatus.revoked);
        pm.setDefault(false);
        paymentMethodRepository.save(pm);
        return toResponse(pm);
    }

    private PaymentMethod getEntity(String pmId) {
        return paymentMethodRepository.findById(pmId)
                .orElseThrow(() -> new BusinessException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found: " + pmId));
    }

    private PaymentMethodResponse toResponse(PaymentMethod pm) {
        return new PaymentMethodResponse(pm.getId(), pm.getCustomerId(), pm.getType(),
                pm.getProvider(), pm.getCardBrand(), pm.getCardLast4(),
                pm.getExpMonth(), pm.getExpYear(), pm.getStatus(), pm.isDefault(), pm.getCreatedAt());
    }
}
