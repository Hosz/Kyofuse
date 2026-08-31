ALTER TABLE users
    ADD COLUMN google_id VARCHAR(255);

ALTER TABLE users
    ADD CONSTRAINT uk_users_google_id UNIQUE (google_id);

CREATE INDEX idx_users_google_id ON users(google_id);

ALTER TABLE users
    ADD COLUMN has_custom_password BOOLEAN NOT NULL DEFAULT TRUE;
