package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class ExchangeRateService {

    private static final int SPREAD_BPS = 50; // 0.5%
    private static final BigDecimal ANOMALY_THRESHOLD = new BigDecimal("0.10"); // 10% deviation

    private static final Map<String, BigDecimal> BASE_RATES = Map.of(
            "USD_EUR", new BigDecimal("0.9200"),
            "USD_GBP", new BigDecimal("0.7900"),
            "EUR_USD", new BigDecimal("1.0870"),
            "EUR_GBP", new BigDecimal("0.8587"),
            "GBP_USD", new BigDecimal("1.2658"),
            "GBP_EUR", new BigDecimal("1.1647")
    );

    public record QuoteResult(BigDecimal rate, int spreadBps, BigDecimal targetAmount) {}

    public QuoteResult getQuote(String sourceCurrency, String targetCurrency, long sourceAmountCents) {
        String pair = sourceCurrency.toUpperCase() + "_" + targetCurrency.toUpperCase();
        BigDecimal baseRate = BASE_RATES.get(pair);
        if (baseRate == null) {
            throw new BusinessException("UNSUPPORTED_PAIR", "Currency pair not supported: " + pair);
        }

        if (isAnomalous(pair, baseRate)) {
            throw new BusinessException("RATE_ANOMALY", "Exchange rate anomaly detected for " + pair + ", operation blocked");
        }

        BigDecimal spreadMultiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(SPREAD_BPS).divide(BigDecimal.valueOf(10000), 6, RoundingMode.HALF_UP));
        BigDecimal effectiveRate = baseRate.multiply(spreadMultiplier).setScale(8, RoundingMode.HALF_UP);

        BigDecimal sourceAmount = BigDecimal.valueOf(sourceAmountCents);
        BigDecimal targetAmount = sourceAmount.multiply(effectiveRate).setScale(0, RoundingMode.DOWN);

        return new QuoteResult(effectiveRate, SPREAD_BPS, targetAmount);
    }

    private boolean isAnomalous(String pair, BigDecimal currentRate) {
        BigDecimal expected = BASE_RATES.get(pair);
        if (expected == null || expected.compareTo(BigDecimal.ZERO) == 0) return false;
        BigDecimal deviation = currentRate.subtract(expected).abs()
                .divide(expected, 4, RoundingMode.HALF_UP);
        return deviation.compareTo(ANOMALY_THRESHOLD) > 0;
    }
}
