package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.Customer;
import com.nexus.recur.domain.repository.CustomerRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import com.nexus.recur.interfaces.rest.dto.CustomerDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final IdGenerator idGenerator;

    public CustomerService(CustomerRepository customerRepository, IdGenerator idGenerator) {
        this.customerRepository = customerRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setId(idGenerator.next("cus"));
        customer.setMerchantId(request.merchantId());
        customer.setExternalCustomerId(request.externalCustomerId());
        customer.setEmail(request.email());
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setAddressJson(request.addressJson());
        customer.setTaxId(request.taxId());
        customer.setMetadataJson(request.metadataJson());
        customerRepository.save(customer);
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(String customerId) {
        return toResponse(getEntity(customerId));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(String merchantId, int page, int limit) {
        return customerRepository.findByMerchantId(merchantId, PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1)))
                .map(this::toResponse);
    }

    @Transactional
    public CustomerResponse update(String customerId, UpdateCustomerRequest request) {
        Customer customer = getEntity(customerId);
        if (request.email() != null) customer.setEmail(request.email());
        if (request.name() != null) customer.setName(request.name());
        if (request.phone() != null) customer.setPhone(request.phone());
        if (request.addressJson() != null) customer.setAddressJson(request.addressJson());
        if (request.taxId() != null) customer.setTaxId(request.taxId());
        if (request.metadataJson() != null) customer.setMetadataJson(request.metadataJson());
        customerRepository.save(customer);
        return toResponse(customer);
    }

    public Customer getEntity(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found: " + customerId));
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getMerchantId(), c.getExternalCustomerId(),
                c.getEmail(), c.getName(), c.getPhone(), c.getAddressJson(), c.getTaxId(),
                c.getMetadataJson(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
