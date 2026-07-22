package com.nexus.recur.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class DashboardDtos {

    public record DashboardStatsResponse(
            int activeSubscriptions,
            int trialingSubscriptions,
            int totalSubscriptions,
            Map<String, Integer> subscriptionsByStatus,
            BigDecimal mrr,
            String mrrCurrency,
            BigDecimal chargeSuccessRate,
            int totalCharges,
            Map<String, BigDecimal> revenueByCurrency,
            List<WalletBalance> walletBalances,
            List<PendingAction> pendingActions,
            List<EventSummary> recentEvents
    ) {}

    public record WalletBalance(String currency, BigDecimal balance, BigDecimal pendingBalance) {}

    public record PendingAction(String subscriptionId, String userId, String status,
                                 OffsetDateTime currentPeriodEnd, String actionType) {}

    public record EventSummary(String id, String subscriptionId, String eventType,
                                String source, OffsetDateTime createdAt) {}
}
