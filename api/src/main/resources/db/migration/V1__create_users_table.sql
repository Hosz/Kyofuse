CREATE TABLE users (
    id UUID PRIMARY KEY,

    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(120) NOT NULL,

    email VARCHAR(160) NOT NULL,
    username VARCHAR(40) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    deactivated_at TIMESTAMPTZ,
    deletion_scheduled_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);