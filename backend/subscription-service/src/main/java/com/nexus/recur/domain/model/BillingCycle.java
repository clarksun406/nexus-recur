package com.nexus.recur.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;

public enum BillingCycle {
    monthly("monthly"),
    quarterly("quarterly"),
    six_month("6month"),
    annual("annual");

    private final String value;

    BillingCycle(String value) {
        this.value = value;
    }

    public OffsetDateTime addTo(OffsetDateTime start) {
        return switch (this) {
            case monthly -> start.plusMonths(1);
            case quarterly -> start.plusMonths(3);
            case six_month -> start.plusMonths(6);
            case annual -> start.plusYears(1);
        };
    }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static BillingCycle from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("billing cycle is required");
        }
        for (BillingCycle cycle : values()) {
            if (cycle.value.equalsIgnoreCase(value) || cycle.name().equalsIgnoreCase(value)) {
                return cycle;
            }
        }
        if ("3month".equalsIgnoreCase(value) || "three_month".equalsIgnoreCase(value)) {
            return quarterly;
        }
        if ("yearly".equalsIgnoreCase(value)) {
            return annual;
        }
        throw new IllegalArgumentException("unsupported billing cycle: " + value);
    }
}
