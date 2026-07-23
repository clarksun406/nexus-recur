package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.FxTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxTransactionRepository extends JpaRepository<FxTransaction, String> {
    Page<FxTransaction> findByMerchantId(String merchantId, Pageable pageable);
    Page<FxTransaction> findBySourceWalletId(String sourceWalletId, Pageable pageable);
}
