ALTER TABLE subscriptions ADD COLUMN next_retry_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_next_retry ON subscriptions (status, next_retry_at);
