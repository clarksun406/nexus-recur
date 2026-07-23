package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.PaymentOrder;
import com.nexus.recur.domain.model.PaymentOrderStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, String> {
    Page<PaymentOrder> findByMerchantId(String merchantId, Pageable pageable);
    Page<PaymentOrder> findByStatus(PaymentOrderStatus status, Pageable pageable);
    List<PaymentOrder> findByWalletId(String walletId);
}
