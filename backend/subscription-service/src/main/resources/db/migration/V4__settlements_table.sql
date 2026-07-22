CREATE TABLE settlements (
    id VARCHAR(32) PRIMARY KEY,
    merchant_id VARCHAR(64) NOT NULL,
    wallet_id VARCHAR(32) NOT NULL,
    amount_cents BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    bank_account VARCHAR(256),
    status VARCHAR(16) NOT NULL,
    approved_by VARCHAR(64),
    approved_at TIMESTAMP WITH TIME ZONE,
    background_refs TEXT,
    rejection_reason VARCHAR(256),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_settlement_merchant ON settlements (merchant_id);
CREATE INDEX idx_settlement_status ON settlements (status);
