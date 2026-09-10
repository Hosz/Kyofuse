CREATE TABLE community_invites (
    id UUID PRIMARY KEY,

    community_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,

    status VARCHAR(40) NOT NULL,
    message VARCHAR(500),

    canceled_by UUID,
    cancellation_reason VARCHAR(500),

    created_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    canceled_at TIMESTAMPTZ,

    CONSTRAINT fk_community_invites_community
        FOREIGN KEY (community_id)
        REFERENCES communities(id) ON DELETE CASCADE,

    CONSTRAINT fk_community_invites_sender
        FOREIGN KEY (sender_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_community_invites_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_community_invites_canceled_by
        FOREIGN KEY (canceled_by)
        REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_community_invites_sender_receiver
        CHECK (sender_id <> receiver_id)
);

CREATE INDEX idx_community_invites_community_id
    ON community_invites(community_id);

CREATE INDEX idx_community_invites_sender_id
    ON community_invites(sender_id);

CREATE INDEX idx_community_invites_receiver_id
    ON community_invites(receiver_id);

CREATE INDEX idx_community_invites_status
    ON community_invites(status);

CREATE INDEX idx_community_invites_created_at
    ON community_invites(created_at);

CREATE INDEX idx_community_invites_receiver_status_created_at
    ON community_invites(receiver_id, status, created_at);

CREATE UNIQUE INDEX uk_community_invites_pending
    ON community_invites(community_id, receiver_id)
    WHERE status = 'PENDING';
