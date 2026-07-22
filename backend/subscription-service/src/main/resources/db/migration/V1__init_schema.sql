-- V1: Initial schema for subscription service

-- Subscription plans
CREATE TABLE subscription_plans (
    id                     VARCHAR(32) PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    description            TEXT,
    product_id             VARCHAR(64),
    billing_cycle          VARCHAR(32) NOT NULL,
    price_cents            INTEGER NOT NULL,
    currency               VARCHAR(3) NOT NULL DEFAULT 'USD',
    trial_days             INTEGER NOT NULL DEFAULT 0,
    features_json          TEXT,
    status                 VARCHAR(16) NOT NULL DEFAULT 'draft',
    billing_type           VARCHAR(16) NOT NULL DEFAULT 'flat_rate',
    metered_config_json    TEXT,
    tax_mode               VARCHAR(16) NOT NULL DEFAULT 'none',
    tax_category           VARCHAR(32),
    license_enabled        BOOLEAN NOT NULL DEFAULT FALSE,
    license_instance_limit INTEGER NOT NULL DEFAULT 1,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Subscriptions
CREATE TABLE subscriptions (
    id                     VARCHAR(32) PRIMARY KEY,
    user_id                VARCHAR(64) NOT NULL,
    plan_id                VARCHAR(32) NOT NULL,
    external_sub_id        VARCHAR(64),
    external_customer_id   VARCHAR(64),
    status                 VARCHAR(32) NOT NULL DEFAULT 'pending',
    current_period_start  TIMESTAMP WITH TIME ZONE,
    current_period_end     TIMESTAMP WITH TIME ZONE,
    trial_end_at           TIMESTAMP WITH TIME ZONE,
    cancel_at_period_end   BOOLEAN NOT NULL DEFAULT FALSE,
    canceled_at            TIMESTAMP WITH TIME ZONE,
    cancel_reason          VARCHAR(255),
    paused_at              TIMESTAMP WITH TIME ZONE,
    metadata_json          TEXT,
    retry_count            INTEGER NOT NULL DEFAULT 0,
    last_decline_code      VARCHAR(64),
    payment_method_id      VARCHAR(64),
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_user_id ON subscriptions(user_id);
CREATE INDEX idx_status ON subscriptions(status);
CREATE INDEX idx_period_end ON subscriptions(current_period_end);

-- Subscription invoices
CREATE TABLE subscription_invoices (
    id                     VARCHAR(32) PRIMARY KEY,
    subscription_id        VARCHAR(32) NOT NULL,
    external_transaction_id VARCHAR(64),
    period_start           TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end             TIMESTAMP WITH TIME ZONE NOT NULL,
    amount_cents           INTEGER NOT NULL,
    currency               VARCHAR(3) NOT NULL DEFAULT 'USD',
    status                 VARCHAR(16) NOT NULL DEFAULT 'pending',
    payment_method         VARCHAR(32),
    paid_at                TIMESTAMP WITH TIME ZONE,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_sub_period UNIQUE (subscription_id, period_start, period_end)
);
CREATE INDEX idx_subscription ON subscription_invoices(subscription_id);

-- Subscription events (audit log)
CREATE TABLE subscription_events (
    id                     VARCHAR(32) PRIMARY KEY,
    subscription_id        VARCHAR(32) NOT NULL,
    event_type             VARCHAR(64) NOT NULL,
    source                 VARCHAR(32) NOT NULL DEFAULT 'webhook',
    raw_payload            TEXT,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_event_subscription ON subscription_events(subscription_id);
CREATE INDEX idx_event_created ON subscription_events(created_at);

-- API keys
CREATE TABLE api_keys (
    id              VARCHAR(32) PRIMARY KEY,
    key_id          VARCHAR(32) NOT NULL,
    key_hash        VARCHAR(100) NOT NULL,
    key_prefix      VARCHAR(16) NOT NULL,
    scope           VARCHAR(24) NOT NULL DEFAULT 'full_access',
    user_id         VARCHAR(64) NOT NULL,
    merchant_id     VARCHAR(64),
    status          VARCHAR(16) NOT NULL DEFAULT 'active',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_used_at    TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_api_key_id ON api_keys(key_id);
CREATE INDEX idx_api_key_user ON api_keys(user_id);

-- Webhook endpoints (outbound)
CREATE TABLE webhook_endpoints (
    id                 VARCHAR(32) PRIMARY KEY,
    merchant_id        VARCHAR(64),
    url                VARCHAR(512) NOT NULL,
    secret             VARCHAR(128) NOT NULL,
    subscribed_events   TEXT,
    status             VARCHAR(16) NOT NULL DEFAULT 'active',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_webhook_endpoint_merchant ON webhook_endpoints(merchant_id);
CREATE INDEX idx_webhook_endpoint_status ON webhook_endpoints(status);

-- Webhook deliveries (outbound delivery tracking)
CREATE TABLE webhook_deliveries (
    id                 VARCHAR(32) PRIMARY KEY,
    endpoint_id        VARCHAR(32) NOT NULL,
    event_type         VARCHAR(64) NOT NULL,
    payload            TEXT NOT NULL,
    status             VARCHAR(16) NOT NULL DEFAULT 'pending',
    attempts           INTEGER NOT NULL DEFAULT 0,
    max_attempts        INTEGER NOT NULL DEFAULT 6,
    next_retry_at       TIMESTAMP WITH TIME ZONE,
    response_code       INTEGER,
    response_message    TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_webhook_delivery_endpoint ON webhook_deliveries(endpoint_id);
CREATE INDEX idx_webhook_delivery_retry ON webhook_deliveries(status, next_retry_at);
