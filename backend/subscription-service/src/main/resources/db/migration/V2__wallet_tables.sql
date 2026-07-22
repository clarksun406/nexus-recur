-- V2: Wallet tables for multi-currency wallet management

CREATE TABLE wallets (
    id                     VARCHAR(32) PRIMARY KEY,
    merchant_id            VARCHAR(64),
    currency               VARCHAR(3) NOT NULL,
    balance_cents          INTEGER NOT NULL,
    pending_balance_cents  INTEGER NOT NULL,
    status                 VARCHAR(16) NOT NULL,
    created_at             TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at             TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE wallet_transactions (
    id              VARCHAR(32) PRIMARY KEY,
    wallet_id       VARCHAR(32) NOT NULL,
    type            VARCHAR(32) NOT NULL,
    amount_cents    INTEGER NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    description     TEXT,
    reference_type  VARCHAR(32),
    reference_id    VARCHAR(64),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
