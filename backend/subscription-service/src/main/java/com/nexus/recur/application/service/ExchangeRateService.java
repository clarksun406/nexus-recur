package com.nexus.recur.application.service;

import com.nexus.recur.application.common.BusinessException;
import com.nexus.recur.domain.model.SupportedCurrencies;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class ExchangeRateService {

    private static final int SPREAD_BPS = 50;
    private static final BigDecimal ANOMALY_THRESHOLD = new BigDecimal("0.10");

    private static final Map<String, BigDecimal> USD_RATES = Map.ofEntries(
            Map.entry("USD", BigDecimal.ONE),
            Map.entry("EUR", new BigDecimal("0.9200")),
            Map.entry("GBP", new BigDecimal("0.7900")),
            Map.entry("CAD", new BigDecimal("1.3650")),
            Map.entry("AUD", new BigDecimal("1.5200")),
            Map.entry("JPY", new BigDecimal("154.50")),
            Map.entry("SGD", new BigDecimal("1.3450")),
            Map.entry("HKD", new BigDecimal("7.8100")),
            Map.entry("CHF", new BigDecimal("0.8850")),
            Map.entry("SEK", new BigDecimal("10.650")),
            Map.entry("NOK", new BigDecimal("10.850")),
            Map.entry("DKK", new BigDecimal("6.8600")),
            Map.entry("NZD", new BigDecimal("1.6400")),
            Map.entry("CNY", new BigDecimal("7.2500")),
            Map.entry("INR", new BigDecimal("83.500")),
            Map.entry("BRL", new BigDecimal("5.4500")),
            Map.entry("MXN", new BigDecimal("17.150")),
            Map.entry("ZAR", new BigDecimal("18.200")),
            Map.entry("KRW", new BigDecimal("1345.0")),
            Map.entry("TWD", new BigDecimal("32.400")),
            Map.entry("PLN", new BigDecimal("3.9800")),
            Map.entry("THB", new BigDecimal("36.200")),
            Map.entry("ILS", new BigDecimal("3.7200"))
    );

    public record QuoteResult(BigDecimal rate, int spreadBps, BigDecimal targetAmount) {}

    public QuoteResult getQuote(String sourceCurrency, String targetCurrency, long sourceAmountCents) {
        String src = sourceCurrency.toUpperCase();
        String tgt = targetCurrency.toUpperCase();
        SupportedCurrencies.validate(src);
        SupportedCurrencies.validate(tgt);
        if (src.equals(tgt)) {
            throw new BusinessException("SAME_CURRENCY", "Source and target currency are the same: " + src);
        }

        BigDecimal baseRate = crossRate(src, tgt);

        BigDecimal spreadMultiplier = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(SPREAD_BPS).divide(BigDecimal.valueOf(10000), 6, RoundingMode.HALF_UP));
        BigDecimal effectiveRate = baseRate.multiply(spreadMultiplier).setScale(8, RoundingMode.HALF_UP);

        BigDecimal sourceAmount = BigDecimal.valueOf(sourceAmountCents);
        BigDecimal targetAmount = sourceAmount.multiply(effectiveRate).setScale(0, RoundingMode.DOWN);

        return new QuoteResult(effectiveRate, SPREAD_BPS, targetAmount);
    }

    private BigDecimal crossRate(String from, String to) {
        BigDecimal fromUsd = USD_RATES.get(from);
        BigDecimal toUsd = USD_RATES.get(to);
        return toUsd.divide(fromUsd, 8, RoundingMode.HALF_UP);
    }
}
