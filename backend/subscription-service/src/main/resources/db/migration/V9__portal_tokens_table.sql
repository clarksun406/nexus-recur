-- V9: Portal tokens table

CREATE TABLE portal_tokens (
    id VARCHAR(32) PRIMARY KEY,
    customer_id VARCHAR(32) NOT NULL,
    token VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_portal_token UNIQUE (token)
);
CREATE INDEX idx_portal_customer ON portal_tokens(customer_id);
