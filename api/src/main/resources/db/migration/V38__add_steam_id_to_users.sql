ALTER TABLE users
    ADD COLUMN steam_id VARCHAR(30);

ALTER TABLE users
    ADD CONSTRAINT uk_users_steam_id UNIQUE (steam_id);

CREATE INDEX idx_users_steam_id ON users(steam_id);
