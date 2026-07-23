package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.Refund;
import com.nexus.recur.domain.model.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, String> {
    Page<Refund> findByMerchantId(String merchantId, Pageable pageable);
    List<Refund> findByInvoiceId(String invoiceId);
    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);
}
