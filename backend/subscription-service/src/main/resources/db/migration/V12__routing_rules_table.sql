CREATE TABLE routing_rules (
    id              VARCHAR(32) PRIMARY KEY,
    merchant_id     VARCHAR(64) NOT NULL,
    provider_name   VARCHAR(64) NOT NULL,
    priority        INTEGER NOT NULL DEFAULT 0,
    currency        VARCHAR(3),
    min_amount_cents BIGINT,
    max_amount_cents BIGINT,
    region          VARCHAR(64),
    weight          INTEGER NOT NULL DEFAULT 1,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    strategy        VARCHAR(32) NOT NULL DEFAULT 'priority',
    success_rate    DOUBLE PRECISION NOT NULL DEFAULT 0.95,
    cost_percentage DOUBLE PRECISION NOT NULL DEFAULT 2.9,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_routing_merchant ON routing_rules(merchant_id);
CREATE INDEX idx_routing_provider ON routing_rules(provider_name);
