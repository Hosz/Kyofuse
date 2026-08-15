CREATE TABLE community_join_requests (
    id UUID PRIMARY KEY,

    community_id UUID NOT NULL,
    requester_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_community_join_requests_community
        FOREIGN KEY (community_id)
            REFERENCES communities(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_community_join_requests_requester
        FOREIGN KEY (requester_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_community_join_requests
        UNIQUE (community_id, requester_id)
);

CREATE INDEX idx_community_join_requests_community_id
    ON community_join_requests(community_id);

CREATE INDEX idx_community_join_requests_requester_id
    ON community_join_requests(requester_id);
