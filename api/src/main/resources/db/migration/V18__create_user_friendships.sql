CREATE TABLE user_friendships
(
    id UUID PRIMARY KEY,

    user_one_id UUID NOT NULL,
    user_two_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_friendships_user_one
        FOREIGN KEY (user_one_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_friendships_user_two
        FOREIGN KEY (user_two_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_user_friendships_self
        CHECK (user_one_id <> user_two_id),

    CONSTRAINT uk_user_friendships
        UNIQUE (user_one_id, user_two_id)
);

CREATE INDEX idx_user_friendships_user_one
    ON user_friendships(user_one_id);

CREATE INDEX idx_user_friendships_user_two
    ON user_friendships(user_two_id);