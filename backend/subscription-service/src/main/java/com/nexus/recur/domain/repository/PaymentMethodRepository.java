package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.PaymentMethod;
import com.nexus.recur.domain.model.PaymentMethodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    List<PaymentMethod> findByCustomerId(String customerId);
    List<PaymentMethod> findByCustomerIdAndStatus(String customerId, PaymentMethodStatus status);
}
