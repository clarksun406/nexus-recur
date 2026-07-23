-- F12: Same-currency payment orders
CREATE TABLE payment_orders (
    id VARCHAR(32) PRIMARY KEY,
    merchant_id VARCHAR(64) NOT NULL,
    wallet_id VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount_cents BIGINT NOT NULL,
    method VARCHAR(16) NOT NULL,
    beneficiary_name VARCHAR(255) NOT NULL,
    beneficiary_account VARCHAR(255) NOT NULL,
    beneficiary_bank VARCHAR(255),
    beneficiary_country VARCHAR(3),
    purpose TEXT,
    status VARCHAR(24) NOT NULL DEFAULT 'pending_screening',
    sanctions_result VARCHAR(16),
    sanctions_checked_at TIMESTAMP WITH TIME ZONE,
    approved_by VARCHAR(64),
    approved_at TIMESTAMP WITH TIME ZONE,
    reference_number VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_po_merchant ON payment_orders(merchant_id);
CREATE INDEX idx_po_wallet ON payment_orders(wallet_id);
CREATE INDEX idx_po_status ON payment_orders(status);
