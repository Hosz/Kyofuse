ALTER TABLE email_verification_tokens
    ADD COLUMN pending_email VARCHAR(500),
    ADD COLUMN pending_email_index VARCHAR(64),
    ADD COLUMN token_type VARCHAR(30) NOT NULL DEFAULT 'REGISTRATION';

CREATE INDEX idx_email_verification_tokens_pending_email_index
    ON email_verification_tokens(pending_email_index);

ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_email_index;
DROP INDEX IF EXISTS uk_users_email_index;
CREATE UNIQUE INDEX uk_users_email_index ON users (email_index) WHERE email_verified = true;
CREATE INDEX IF NOT EXISTS idx_users_email_index ON users (email_index);

ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_username;
DROP INDEX IF EXISTS uk_users_username;
CREATE UNIQUE INDEX uk_users_username ON users (LOWER(username)) WHERE email_verified = true;
CREATE INDEX IF NOT EXISTS idx_users_username_lower ON users (LOWER(username));

ALTER TABLE gamer_profiles DROP CONSTRAINT IF EXISTS fk_gamer_profiles_user;
ALTER TABLE gamer_profiles
    ADD CONSTRAINT fk_gamer_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

