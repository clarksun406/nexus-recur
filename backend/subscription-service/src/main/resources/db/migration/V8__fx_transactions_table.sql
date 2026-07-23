-- V8: FX transactions table

CREATE TABLE fx_transactions (
    id VARCHAR(32) PRIMARY KEY,
    merchant_id VARCHAR(64) NOT NULL,
    source_wallet_id VARCHAR(32) NOT NULL,
    target_wallet_id VARCHAR(32) NOT NULL,
    source_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    source_amount_cents BIGINT NOT NULL,
    target_amount_cents BIGINT NOT NULL,
    exchange_rate NUMERIC(18,8) NOT NULL,
    spread_bps INTEGER NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    failure_reason VARCHAR(256),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_fx_merchant ON fx_transactions(merchant_id);
CREATE INDEX idx_fx_wallet ON fx_transactions(source_wallet_id);
