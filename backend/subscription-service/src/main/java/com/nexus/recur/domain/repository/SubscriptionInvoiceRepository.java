package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.InvoiceStatus;
import com.nexus.recur.domain.model.SubscriptionInvoice;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionInvoiceRepository extends JpaRepository<SubscriptionInvoice, String> {
    Page<SubscriptionInvoice> findBySubscriptionId(String subscriptionId, Pageable pageable);
    boolean existsBySubscriptionIdAndPeriodStartAndPeriodEnd(String subscriptionId, OffsetDateTime periodStart, OffsetDateTime periodEnd);
    long countByStatus(InvoiceStatus status);
    List<SubscriptionInvoice> findByMerchantIdAndCreatedAtBetween(String merchantId, OffsetDateTime start, OffsetDateTime end);
    List<SubscriptionInvoice> findByStatusAndCreatedAtBetween(InvoiceStatus status, OffsetDateTime start, OffsetDateTime end);
    List<SubscriptionInvoice> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
