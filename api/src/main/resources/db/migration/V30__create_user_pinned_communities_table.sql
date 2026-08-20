-- Comunidades que o usuário escolheu fixar no feed da página inicial, no mesmo espírito
-- das abas de comunidade do X: a escolha é pessoal e acompanha a conta, não o navegador.
CREATE TABLE user_pinned_communities (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    community_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_user_pinned_communities_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_user_pinned_communities_community
        FOREIGN KEY (community_id)
            REFERENCES communities(id),

    CONSTRAINT uk_user_pinned_communities
        UNIQUE (user_id, community_id)
);

CREATE INDEX idx_user_pinned_communities_user_id
    ON user_pinned_communities(user_id);

CREATE INDEX idx_user_pinned_communities_community_id
    ON user_pinned_communities(community_id);
