package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.SanctionsResult;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SanctionsScreeningService {

    private static final Set<String> SANCTIONED_COUNTRIES = Set.of(
            "KP", "IR", "SY", "CU", "VE", "RU", "BY", "MM", "AF", "SD", "SO", "YE", "LY", "ZW"
    );

    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
            "sanctioned", "blocked", "terrorist", "ofac", "sdn"
    );

    public SanctionsResult screen(String beneficiaryName, String beneficiaryCountry) {
        if (beneficiaryCountry != null && SANCTIONED_COUNTRIES.contains(beneficiaryCountry.toUpperCase())) {
            return SanctionsResult.blocked;
        }
        if (beneficiaryName != null) {
            String lower = beneficiaryName.toLowerCase();
            for (String keyword : BLOCKED_KEYWORDS) {
                if (lower.contains(keyword)) return SanctionsResult.blocked;
            }
        }
        return SanctionsResult.pass;
    }
}
