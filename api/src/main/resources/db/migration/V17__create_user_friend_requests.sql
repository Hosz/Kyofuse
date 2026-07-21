CREATE TABLE user_friend_requests
(
    id UUID PRIMARY KEY,

    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_friend_requests_sender
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_friend_requests_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_user_friend_requests_self
        CHECK (sender_id <> receiver_id),

    CONSTRAINT uk_user_friend_requests
        UNIQUE (sender_id, receiver_id)
);

CREATE INDEX idx_user_friend_requests_sender
    ON user_friend_requests(sender_id);

CREATE INDEX idx_user_friend_requests_receiver
    ON user_friend_requests(receiver_id);