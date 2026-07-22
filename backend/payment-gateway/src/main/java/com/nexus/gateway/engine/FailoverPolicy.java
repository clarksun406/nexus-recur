package com.nexus.gateway.engine;

import com.nexus.gateway.provider.PaymentProvider;

import java.util.Set;

public class FailoverPolicy {

    private static final Set<String> HARD_DECLINE_CODES = Set.of(
            "card_declined", "insufficient_funds", "expired_card",
            "do_not_honor", "fraudulent", "lost_card", "stolen_card"
    );

    public boolean isRetryable(String declineCode) {
        return declineCode == null || !HARD_DECLINE_CODES.contains(declineCode);
    }
}
