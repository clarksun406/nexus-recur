package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
}
