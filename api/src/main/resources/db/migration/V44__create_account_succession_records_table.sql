CREATE TABLE account_succession_records (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    successor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    previous_member_type VARCHAR(40),
    previous_role_in_team VARCHAR(80),
    previous_role VARCHAR(40),
    previous_successor_member_type VARCHAR(40),
    previous_successor_role VARCHAR(40),
    was_owner BOOLEAN NOT NULL DEFAULT FALSE,
    previous_status VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_account_succession_records_user_id ON account_succession_records(user_id);
