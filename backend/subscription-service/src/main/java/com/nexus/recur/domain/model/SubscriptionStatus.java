package com.nexus.recur.domain.model;

public enum SubscriptionStatus {
    pending,
    trialing,
    active,
    past_due,
    paused,
    scheduled_cancel,
    canceled,
    expired
}
