CREATE TABLE conversation_members (
    id UUID PRIMARY KEY,

    conversation_id UUID NOT NULL,
    user_id UUID NOT NULL,

    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    joined_at TIMESTAMPTZ NOT NULL,
    left_at TIMESTAMPTZ,
    last_read_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_conversation_members_conversation
        FOREIGN KEY (conversation_id)
            REFERENCES conversations(id),

    CONSTRAINT fk_conversation_members_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT uk_conversation_members
        UNIQUE (conversation_id, user_id)
);

CREATE INDEX idx_conversation_members_conversation_id
    ON conversation_members(conversation_id);

CREATE INDEX idx_conversation_members_user_id
    ON conversation_members(user_id);

CREATE INDEX idx_conversation_members_status
    ON conversation_members(status);
