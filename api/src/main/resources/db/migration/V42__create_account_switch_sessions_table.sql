CREATE TABLE account_switch_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id VARCHAR(100) NOT NULL,
    switch_token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_account_switch_user_id ON account_switch_sessions(user_id);
CREATE INDEX idx_account_switch_device_id ON account_switch_sessions(device_id);
CREATE INDEX idx_account_switch_token_hash ON account_switch_sessions(switch_token_hash);
