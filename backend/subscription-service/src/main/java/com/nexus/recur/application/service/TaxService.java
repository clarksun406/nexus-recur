package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.SubscriptionPlan;
import com.nexus.recur.domain.model.TaxMode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxService {

    public TaxResult calculateTax(SubscriptionPlan plan, String country) {
        if (plan.getTaxMode() == null || plan.getTaxMode() == TaxMode.none) {
            return new TaxResult(BigDecimal.valueOf(plan.getPriceCents(), 2), BigDecimal.ZERO, BigDecimal.valueOf(plan.getPriceCents(), 2), "none", BigDecimal.ZERO);
        }

        BigDecimal rate = resolveRate(country);
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

    private BigDecimal resolveRate(String country) {
        if (country == null) return BigDecimal.ZERO;
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
