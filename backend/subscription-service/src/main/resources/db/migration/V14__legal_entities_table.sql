CREATE TABLE legal_entities (
    id                  VARCHAR(32) PRIMARY KEY,
    name                VARCHAR(128) NOT NULL,
    registration_number VARCHAR(64),
    country             VARCHAR(3) NOT NULL DEFAULT 'US',
    tax_id              VARCHAR(64),
    address_json        TEXT,
    bank_account_json   TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'active',
    primary_contact     VARCHAR(128),
    primary_email       VARCHAR(128),
    base_currency       VARCHAR(3) DEFAULT 'USD',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_entity_country ON legal_entities(country);
CREATE INDEX idx_entity_status ON legal_entities(status);

ALTER TABLE merchants ADD COLUMN legal_entity_id VARCHAR(32);
CREATE INDEX idx_merchant_entity ON merchants(legal_entity_id);
