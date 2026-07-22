package com.nexus.recur.infrastructure.webhook;

public final class WebhookEventTypes {
    private WebhookEventTypes() {}

    public static final String CHARGE_SUCCEEDED = "charge.succeeded";
    public static final String CHARGE_FAILED = "charge.failed";
    public static final String SUBSCRIPTION_CREATED = "subscription.created";
    public static final String SUBSCRIPTION_ACTIVATED = "subscription.activated";
    public static final String SUBSCRIPTION_TRIALING = "subscription.trialing";
    public static final String SUBSCRIPTION_PAST_DUE = "subscription.past_due";
    public static final String SUBSCRIPTION_CANCELED = "subscription.canceled";
    public static final String SUBSCRIPTION_RECOVERED = "subscription.recovered";
    public static final String SUBSCRIPTION_PAUSED = "subscription.paused";
    public static final String SUBSCRIPTION_RESUMED = "subscription.resumed";
    public static final String SUBSCRIPTION_SCHEDULED_CANCEL = "subscription.scheduled_cancel";
    public static final String SUBSCRIPTION_EXPIRED = "subscription.expired";
    public static final String SUBSCRIPTION_UPDATED = "subscription.updated";
    public static final String REFUND_SUCCEEDED = "refund.succeeded";
    public static final String REFUND_FAILED = "refund.failed";
}
