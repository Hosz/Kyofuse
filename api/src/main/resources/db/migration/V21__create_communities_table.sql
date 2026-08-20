CREATE TABLE communities (
    id UUID PRIMARY KEY,

    owner_id UUID NOT NULL,
    team_id UUID,

    name VARCHAR(80) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    avatar_url VARCHAR(500),
    banner_url VARCHAR(500),

    visibility VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_communities_owner
        FOREIGN KEY (owner_id)
            REFERENCES users(id),

    CONSTRAINT fk_communities_team
        FOREIGN KEY (team_id)
            REFERENCES teams(id),

    CONSTRAINT uk_communities_slug
        UNIQUE (slug),

    -- Garante no máximo uma Community por Team. Toda Team possui obrigatoriamente
    -- uma Community, criada automaticamente na criação do Team (TeamService) —
    -- esse lado obrigatório não é reforçado por FK/CHECK aqui, apenas a unicidade
    -- (mesmo padrão de gamer_profiles.user_id em relação a users).
    CONSTRAINT uk_communities_team
        UNIQUE (team_id)
);

CREATE INDEX idx_communities_owner_id
    ON communities(owner_id);

CREATE INDEX idx_communities_team_id
    ON communities(team_id);

CREATE INDEX idx_communities_visibility
    ON communities(visibility);

CREATE INDEX idx_communities_status
    ON communities(status);
