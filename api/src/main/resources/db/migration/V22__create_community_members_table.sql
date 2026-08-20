CREATE TABLE community_members (
    id UUID PRIMARY KEY,

    community_id UUID NOT NULL,
    user_id UUID NOT NULL,

    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    joined_at TIMESTAMPTZ NOT NULL,
    left_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_community_members_community
        FOREIGN KEY (community_id)
            REFERENCES communities(id),

    CONSTRAINT fk_community_members_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT uk_community_members
        UNIQUE (community_id, user_id)
);

CREATE INDEX idx_community_members_community_id
    ON community_members(community_id);

CREATE INDEX idx_community_members_user_id
    ON community_members(user_id);

CREATE INDEX idx_community_members_role
    ON community_members(role);

CREATE INDEX idx_community_members_status
    ON community_members(status);
