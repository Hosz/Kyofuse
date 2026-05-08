CREATE TABLE team_members (
    id UUID PRIMARY KEY,

    team_id UUID NOT NULL,
    user_id UUID NOT NULL,

    role_in_team VARCHAR(40),
    member_type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,

    joined_at TIMESTAMPTZ NOT NULL,
    left_at TIMESTAMPTZ,
    assignment_due_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_team_members_team
      FOREIGN KEY (team_id)
          REFERENCES teams(id),

    CONSTRAINT fk_team_members_user
      FOREIGN KEY (user_id)
          REFERENCES users(id)
);

CREATE INDEX idx_team_members_team_id
    ON team_members(team_id);

CREATE INDEX idx_team_members_user_id
    ON team_members(user_id);

CREATE INDEX idx_team_members_role_in_team
    ON team_members(role_in_team);

CREATE INDEX idx_team_members_member_type
    ON team_members(member_type);

CREATE INDEX idx_team_members_status
    ON team_members(status);

CREATE INDEX idx_team_members_assignment_due_at
    ON team_members(assignment_due_at);

CREATE INDEX idx_team_members_team_status
    ON team_members(team_id, status);

CREATE INDEX idx_team_members_team_member_type_status
    ON team_members(team_id, member_type, status);

CREATE UNIQUE INDEX uk_team_members_active_member
    ON team_members(team_id, user_id)
    WHERE status = 'ACTIVE';