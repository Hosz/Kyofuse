CREATE TABLE post_media (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,

    file_key VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),

    content_type VARCHAR(50) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    width INTEGER,
    height INTEGER,
    display_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_post_media_post
        FOREIGN KEY (post_id)
            REFERENCES posts(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_post_media_post_id ON post_media(post_id);
