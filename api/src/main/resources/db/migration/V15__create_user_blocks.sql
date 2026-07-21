CREATE TABLE user_blocks
(
    id UUID PRIMARY KEY,

    blocker_id UUID NOT NULL,
    blocked_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_blocks_blocker
        FOREIGN KEY (blocker_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_blocks_blocked
        FOREIGN KEY (blocked_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_user_blocks_self
        CHECK (blocker_id <> blocked_id),

    CONSTRAINT uk_user_blocks
        UNIQUE (blocker_id, blocked_id)
);

CREATE INDEX idx_user_blocks_blocker
    ON user_blocks(blocker_id);

CREATE INDEX idx_user_blocks_blocked
    ON user_blocks(blocked_id);