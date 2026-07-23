package com.nexus.recur.domain.model;

import java.util.Set;

public final class SupportedCurrencies {
    private SupportedCurrencies() {}

    public static final Set<String> ALL = Set.of(
            "USD", "EUR", "GBP", "CAD", "AUD", "JPY",
            "SGD", "HKD", "CHF", "SEK", "NOK", "DKK", "NZD",
            "CNY", "INR", "BRL", "MXN", "ZAR", "KRW", "TWD",
            "PLN", "THB", "ILS"
    );

    public static boolean isSupported(String code) {
        return code != null && ALL.contains(code.toUpperCase());
    }

    public static void validate(String code) {
        if (!isSupported(code)) {
            throw new com.nexus.recur.application.common.BusinessException(
                    "UNSUPPORTED_CURRENCY", "Currency not supported: " + code);
        }
    }
}
