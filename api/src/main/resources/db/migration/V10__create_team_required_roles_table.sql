CREATE TABLE team_required_roles (
    id UUID PRIMARY KEY,

    team_id UUID NOT NULL,
    role_name VARCHAR(40) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_team_required_roles_team
     FOREIGN KEY (team_id)
         REFERENCES teams(id),

    CONSTRAINT uk_team_required_roles_team_role
     UNIQUE (team_id, role_name)
);

CREATE INDEX idx_team_required_roles_role_name
    ON team_required_roles(role_name);