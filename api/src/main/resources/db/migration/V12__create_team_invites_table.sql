CREATE TABLE team_invites (
    id UUID PRIMARY KEY,

    team_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,

    status VARCHAR(40) NOT NULL,
    message VARCHAR(500),

    proposed_member_type VARCHAR(40),
    proposed_role_in_team VARCHAR(40),

    canceled_by UUID,
    cancellation_reason VARCHAR(500),

    created_at TIMESTAMPTZ NOT NULL,
    responded_at TIMESTAMPTZ,
    canceled_at TIMESTAMPTZ,

    CONSTRAINT fk_team_invites_team
      FOREIGN KEY (team_id)
          REFERENCES teams(id),

    CONSTRAINT fk_team_invites_sender
      FOREIGN KEY (sender_id)
          REFERENCES users(id),

    CONSTRAINT fk_team_invites_receiver
      FOREIGN KEY (receiver_id)
          REFERENCES users(id),

    CONSTRAINT fk_team_invites_canceled_by
      FOREIGN KEY (canceled_by)
          REFERENCES users(id),

    CONSTRAINT chk_team_invites_sender_receiver
      CHECK (sender_id <> receiver_id)
);

CREATE INDEX idx_team_invites_team_id
    ON team_invites(team_id);

CREATE INDEX idx_team_invites_sender_id
    ON team_invites(sender_id);

CREATE INDEX idx_team_invites_receiver_id
    ON team_invites(receiver_id);

CREATE INDEX idx_team_invites_status
    ON team_invites(status);

CREATE INDEX idx_team_invites_created_at
    ON team_invites(created_at);

CREATE INDEX idx_team_invites_canceled_by
    ON team_invites(canceled_by);

CREATE INDEX idx_team_invites_receiver_status_created_at
    ON team_invites(receiver_id, status, created_at);

CREATE UNIQUE INDEX uk_team_invites_pending
    ON team_invites(team_id, receiver_id)
    WHERE status = 'PENDING';