package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Page<Customer> findByMerchantId(String merchantId, Pageable pageable);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByExternalCustomerId(String externalCustomerId);
}
