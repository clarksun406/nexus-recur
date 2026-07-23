package com.nexus.recur.domain.repository;

import com.nexus.recur.domain.model.TaxRate;
import com.nexus.recur.domain.model.TaxRateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxRateRepository extends JpaRepository<TaxRate, String> {
    List<TaxRate> findByCountryAndStatus(String country, TaxRateStatus status);
    List<TaxRate> findByJurisdictionAndStatus(String jurisdiction, TaxRateStatus status);
}
