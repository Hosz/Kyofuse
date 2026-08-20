CREATE TABLE messages (
    id UUID PRIMARY KEY,

    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,

    content VARCHAR(2000) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id)
            REFERENCES conversations(id),

    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id)
            REFERENCES users(id)
);

CREATE INDEX idx_messages_conversation_id
    ON messages(conversation_id);

CREATE INDEX idx_messages_sender_id
    ON messages(sender_id);

CREATE INDEX idx_messages_created_at
    ON messages(created_at);

CREATE INDEX idx_messages_conversation_created_at
    ON messages(conversation_id, created_at);
