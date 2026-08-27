-- Secret TOTP fica cifrado (AES-GCM, mesmo esquema do email) em totp_secret; enquanto
-- totp_enabled for false, o secret é "pendente" (setup feito mas ainda não confirmado
-- com um código válido) e não vale pra exigir 2FA no login.
ALTER TABLE users
    ADD COLUMN totp_secret VARCHAR(500);

ALTER TABLE users
    ADD COLUMN totp_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users
    ADD COLUMN totp_confirmed_at TIMESTAMPTZ;

CREATE TABLE user_recovery_codes (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    code_hash VARCHAR(64) NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_user_recovery_codes_user_id ON user_recovery_codes(user_id);
