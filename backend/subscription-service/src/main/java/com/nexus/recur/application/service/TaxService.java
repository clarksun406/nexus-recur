package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.TaxMode;
import com.nexus.recur.domain.model.TaxRate;
import com.nexus.recur.domain.model.TaxRateStatus;
import com.nexus.recur.domain.repository.TaxRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TaxService {

    private final TaxRateRepository taxRateRepository;

    public TaxService(TaxRateRepository taxRateRepository) {
        this.taxRateRepository = taxRateRepository;
    }

    public TaxResult calculateTax(SubscriptionPlan plan, String country) {
        if (plan.getTaxMode() == null || plan.getTaxMode() == TaxMode.none) {
            return new TaxResult(BigDecimal.valueOf(plan.getPriceCents(), 2), BigDecimal.ZERO, BigDecimal.valueOf(plan.getPriceCents(), 2), "none", BigDecimal.ZERO);
        }

        BigDecimal rate = resolveRate(country, plan.getTaxCategory());
        BigDecimal baseAmount = BigDecimal.valueOf(plan.getPriceCents(), 2);

        if (plan.getTaxMode() == TaxMode.exclusive) {
            BigDecimal tax = baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = baseAmount.add(tax);
            return new TaxResult(baseAmount, tax, total, country != null ? country : "default", rate);
        }

        // inclusive: tax is already in the price
        BigDecimal tax = baseAmount.multiply(rate).divide(BigDecimal.ONE.add(rate), 2, RoundingMode.HALF_UP);
        BigDecimal net = baseAmount.subtract(tax);
        return new TaxResult(net, tax, baseAmount, country != null ? country : "default", rate);
    }

    private BigDecimal resolveRate(String country, String taxCategory) {
        if (country == null) return BigDecimal.ZERO;

        List<TaxRate> dbRates = taxRateRepository.findByCountryAndStatus(country.toUpperCase(), TaxRateStatus.active);
        if (!dbRates.isEmpty()) {
            TaxRate matched = dbRates.stream()
                    .filter(r -> taxCategory != null && taxCategory.equals(r.getTaxCategory()))
                    .findFirst()
                    .orElse(dbRates.get(0));
            return BigDecimal.valueOf(matched.getPercentage()).divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
        }

        return switch (country.toUpperCase()) {
            case "GB" -> new BigDecimal("0.20");
            case "DE", "FR", "IT", "ES", "NL" -> new BigDecimal("0.21");
            case "IE" -> new BigDecimal("0.23");
            case "AU" -> new BigDecimal("0.10");
            case "JP" -> new BigDecimal("0.10");
            case "CA" -> new BigDecimal("0.05");
            default -> BigDecimal.ZERO;
        };
    }

    public record TaxResult(BigDecimal netAmount, BigDecimal taxAmount, BigDecimal totalAmount,
                            String jurisdiction, BigDecimal taxRate) {}
}
