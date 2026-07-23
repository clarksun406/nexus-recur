CREATE TABLE virtual_cards (
    id                  VARCHAR(32) PRIMARY KEY,
    merchant_id         VARCHAR(64) NOT NULL,
    customer_id         VARCHAR(32),
    card_token          VARCHAR(64) NOT NULL,
    last4               VARCHAR(4) NOT NULL,
    exp_month           INTEGER NOT NULL,
    exp_year            INTEGER NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    spending_limit_cents BIGINT NOT NULL DEFAULT 100000,
    spent_cents         BIGINT NOT NULL DEFAULT 0,
    label               VARCHAR(128),
    status              VARCHAR(32) NOT NULL DEFAULT 'active',
    issued_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMP WITH TIME ZONE,
    frozen_at           TIMESTAMP WITH TIME ZONE,
    closed_at           TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_vcard_merchant ON virtual_cards(merchant_id);
CREATE INDEX idx_vcard_customer ON virtual_cards(customer_id);
CREATE INDEX idx_vcard_status ON virtual_cards(status);
