package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.Wallet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, String> {
    List<Wallet> findByMerchantId(String merchantId);
    Optional<Wallet> findByMerchantIdAndCurrency(String merchantId, String currency);
}
