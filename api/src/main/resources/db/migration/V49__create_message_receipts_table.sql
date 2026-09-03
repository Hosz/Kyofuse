CREATE TABLE message_receipts (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    user_id UUID NOT NULL,
    delivered_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_message_receipts_message
      FOREIGN KEY (message_id)
          REFERENCES messages(id)
          ON DELETE CASCADE,

    CONSTRAINT fk_message_receipts_user
      FOREIGN KEY (user_id)
          REFERENCES users(id)
          ON DELETE CASCADE,

    CONSTRAINT uk_message_receipts_message_user
      UNIQUE (message_id, user_id)
);

CREATE INDEX idx_message_receipts_message_id ON message_receipts(message_id);
CREATE INDEX idx_message_receipts_user_id ON message_receipts(user_id);
CREATE INDEX idx_message_receipts_read_at ON message_receipts(read_at);