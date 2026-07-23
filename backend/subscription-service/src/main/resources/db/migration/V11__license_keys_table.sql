-- F28: License Key management
CREATE TABLE license_keys (
    id VARCHAR(32) PRIMARY KEY,
    license_key VARCHAR(64) NOT NULL UNIQUE,
    merchant_id VARCHAR(64) NOT NULL,
    subscription_id VARCHAR(32),
    plan_id VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    device_fingerprint VARCHAR(128),
    max_activations INT NOT NULL DEFAULT 1,
    current_activations INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP WITH TIME ZONE,
    last_validated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_license_key ON license_keys(license_key);
CREATE INDEX idx_license_sub ON license_keys(subscription_id);
CREATE INDEX idx_license_merchant ON license_keys(merchant_id);
