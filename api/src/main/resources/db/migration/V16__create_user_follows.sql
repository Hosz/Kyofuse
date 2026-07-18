CREATE TABLE user_follows
(
    id UUID PRIMARY KEY,

    follower_id UUID NOT NULL,
    followed_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_follows_follower
        FOREIGN KEY (follower_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_follows_followed
        FOREIGN KEY (followed_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_user_follows_self
        CHECK (follower_id <> followed_id),

    CONSTRAINT uk_user_follows
        UNIQUE (follower_id, followed_id)
);

CREATE INDEX idx_user_follows_follower
    ON user_follows(follower_id);

CREATE INDEX idx_user_follows_followed
    ON user_follows(followed_id);

CREATE INDEX idx_user_follows_status
    ON user_follows(status);