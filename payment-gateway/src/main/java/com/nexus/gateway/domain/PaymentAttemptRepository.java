package com.nexus.gateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, String> {
    List<PaymentAttempt> findByPaymentIntentIdOrderByCreatedAtAsc(String paymentIntentId);
}
