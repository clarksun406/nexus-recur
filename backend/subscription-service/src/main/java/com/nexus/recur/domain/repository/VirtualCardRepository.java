package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.VirtualCard;
import com.nexus.recur.domain.model.VirtualCardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualCardRepository extends JpaRepository<VirtualCard, String> {
    Page<VirtualCard> findByMerchantId(String merchantId, Pageable pageable);
    List<VirtualCard> findByCustomerId(String customerId);
    List<VirtualCard> findByMerchantIdAndStatus(String merchantId, VirtualCardStatus status);
}
