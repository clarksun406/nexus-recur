-- V7: Data model completion — new tables + field additions

-- === NEW TABLES ===

CREATE TABLE customers (
    id VARCHAR(32) PRIMARY KEY,
    merchant_id VARCHAR(64) NOT NULL,
    external_customer_id VARCHAR(64),
    email VARCHAR(128),
    name VARCHAR(128),
    phone VARCHAR(32),
    address_json TEXT,
    tax_id VARCHAR(64),
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_customers_merchant ON customers(merchant_id);
CREATE INDEX idx_customers_email ON customers(email);

CREATE TABLE payment_methods (
    id VARCHAR(32) PRIMARY KEY,
    customer_id VARCHAR(32) NOT NULL,
    type VARCHAR(16) NOT NULL,
    provider VARCHAR(32),
    card_brand VARCHAR(16),
    card_last4 VARCHAR(4),
    exp_month INTEGER,
    exp_year INTEGER,
    billing_address_json TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pm_customer ON payment_methods(customer_id);

CREATE TABLE refunds (
    id VARCHAR(32) PRIMARY KEY,
    invoice_id VARCHAR(32) NOT NULL,
    subscription_id VARCHAR(32),
    merchant_id VARCHAR(64) NOT NULL,
    amount_cents INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reason VARCHAR(256),
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    initiated_by VARCHAR(64),
    approved_by VARCHAR(64),
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refunds_merchant ON refunds(merchant_id);
CREATE INDEX idx_refunds_invoice ON refunds(invoice_id);

CREATE TABLE usage_records (
    id VARCHAR(32) PRIMARY KEY,
    subscription_id VARCHAR(32) NOT NULL,
    plan_id VARCHAR(32),
    quantity BIGINT NOT NULL,
    unit_name VARCHAR(32),
    idempotency_key VARCHAR(64),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_usage_idempotency UNIQUE (idempotency_key)
);
CREATE INDEX idx_usage_subscription ON usage_records(subscription_id);

CREATE TABLE invoice_line_items (
    id VARCHAR(32) PRIMARY KEY,
    invoice_id VARCHAR(32) NOT NULL,
    description VARCHAR(256),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price_cents INTEGER NOT NULL,
    amount_cents INTEGER NOT NULL,
    tax_amount_cents INTEGER NOT NULL DEFAULT 0,
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE,
    plan_id VARCHAR(32),
    line_type VARCHAR(16) NOT NULL
);
CREATE INDEX idx_ili_invoice ON invoice_line_items(invoice_id);

CREATE TABLE tax_rates (
    id VARCHAR(32) PRIMARY KEY,
    jurisdiction VARCHAR(64) NOT NULL,
    country VARCHAR(2) NOT NULL,
    state VARCHAR(64),
    percentage INTEGER NOT NULL,
    display_name VARCHAR(64),
    inclusive BOOLEAN NOT NULL DEFAULT FALSE,
    tax_category VARCHAR(32),
    status VARCHAR(16) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tax_country ON tax_rates(country);
CREATE INDEX idx_tax_jurisdiction ON tax_rates(jurisdiction);

CREATE TABLE plan_tiers (
    id VARCHAR(32) PRIMARY KEY,
    plan_id VARCHAR(32) NOT NULL,
    tier_start INTEGER NOT NULL,
    tier_end INTEGER,
    unit_amount_cents INTEGER NOT NULL,
    flat_amount_cents INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_tier_plan ON plan_tiers(plan_id);

CREATE TABLE retry_logs (
    id VARCHAR(32) PRIMARY KEY,
    subscription_id VARCHAR(32) NOT NULL,
    invoice_id VARCHAR(32),
    attempt_number INTEGER NOT NULL,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    decline_code VARCHAR(64),
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_retry_subscription ON retry_logs(subscription_id);

-- === ALTER EXISTING TABLES ===

-- subscriptions
ALTER TABLE subscriptions ADD COLUMN merchant_id VARCHAR(64);
ALTER TABLE subscriptions ADD COLUMN customer_id VARCHAR(32);
ALTER TABLE subscriptions ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1;
ALTER TABLE subscriptions ADD COLUMN start_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE subscriptions ADD COLUMN collection_method VARCHAR(32);
ALTER TABLE subscriptions ADD COLUMN days_until_due INTEGER;
CREATE INDEX idx_sub_merchant ON subscriptions(merchant_id);

-- subscription_invoices
ALTER TABLE subscription_invoices ADD COLUMN subtotal_cents INTEGER;
ALTER TABLE subscription_invoices ADD COLUMN tax_amount_cents INTEGER;
ALTER TABLE subscription_invoices ADD COLUMN total_cents INTEGER;
ALTER TABLE subscription_invoices ADD COLUMN due_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE subscription_invoices ADD COLUMN invoice_number VARCHAR(32);
ALTER TABLE subscription_invoices ADD COLUMN payment_intent_id VARCHAR(64);
ALTER TABLE subscription_invoices ADD COLUMN merchant_id VARCHAR(64);
ALTER TABLE subscription_invoices ADD COLUMN customer_id VARCHAR(32);
ALTER TABLE subscription_invoices ADD COLUMN discount_amount_cents INTEGER;
ALTER TABLE subscription_invoices ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- wallets
ALTER TABLE wallets ADD COLUMN account_number VARCHAR(64);
ALTER TABLE wallets ADD COLUMN routing_number VARCHAR(32);
ALTER TABLE wallets ADD COLUMN iban VARCHAR(34);
ALTER TABLE wallets ADD COLUMN swift_code VARCHAR(11);
ALTER TABLE wallets ADD COLUMN bank_name VARCHAR(64);
ALTER TABLE wallets ADD COLUMN account_holder_name VARCHAR(128);

-- settlements
ALTER TABLE settlements ADD COLUMN exchange_rate NUMERIC(18,8);
ALTER TABLE settlements ADD COLUMN fee_amount_cents BIGINT;
ALTER TABLE settlements ADD COLUMN net_amount_cents BIGINT;
ALTER TABLE settlements ADD COLUMN expected_arrival_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE settlements ADD COLUMN reference_number VARCHAR(64);
ALTER TABLE settlements ADD COLUMN initiated_by VARCHAR(64);
ALTER TABLE settlements ADD COLUMN compliance_status VARCHAR(16);

-- merchants
ALTER TABLE merchants ADD COLUMN company_name VARCHAR(128);
ALTER TABLE merchants ADD COLUMN country VARCHAR(2);
ALTER TABLE merchants ADD COLUMN business_type VARCHAR(32);
ALTER TABLE merchants ADD COLUMN tax_id VARCHAR(64);
ALTER TABLE merchants ADD COLUMN phone VARCHAR(32);
ALTER TABLE merchants ADD COLUMN address_json TEXT;
ALTER TABLE merchants ADD COLUMN website VARCHAR(256);
ALTER TABLE merchants ADD COLUMN industry VARCHAR(64);
ALTER TABLE merchants ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'active';
ALTER TABLE merchants ADD COLUMN default_currency VARCHAR(3);
ALTER TABLE merchants ADD COLUMN kyc_submitted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE merchants ADD COLUMN kyc_rejected_reason VARCHAR(256);
ALTER TABLE merchants ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
