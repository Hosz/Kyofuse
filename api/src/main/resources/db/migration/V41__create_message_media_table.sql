CREATE TABLE message_media (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,

    file_key VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),

    content_type VARCHAR(50) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    width INTEGER,
    height INTEGER,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_message_media_message
        FOREIGN KEY (message_id)
            REFERENCES messages(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_message_media_message_id ON message_media(message_id);
