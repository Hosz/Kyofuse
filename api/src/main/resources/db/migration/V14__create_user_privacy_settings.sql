CREATE TABLE user_privacy_settings
(
    user_id UUID PRIMARY KEY,

    profile_visibility VARCHAR(40) NOT NULL,
    posts_visibility VARCHAR(40) NOT NULL,
    likes_visibility VARCHAR(40) NOT NULL,
    reposts_visibility VARCHAR(40) NOT NULL,
    friends_visibility VARCHAR(40) NOT NULL,
    followers_visibility VARCHAR(40) NOT NULL,
    following_visibility VARCHAR(40) NOT NULL,

    message_permission VARCHAR(40) NOT NULL,
    friend_request_permission VARCHAR(40) NOT NULL,
    follow_permission VARCHAR(40) NOT NULL,
    team_invite_permission VARCHAR(40) NOT NULL,
    duo_invite_permission VARCHAR(40) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_privacy_settings_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_user_privacy_settings_user
    ON user_privacy_settings(user_id);