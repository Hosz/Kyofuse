ALTER TABLE posts
    ADD COLUMN community_id UUID;

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_community
        FOREIGN KEY (community_id)
            REFERENCES communities(id);

-- Posts de comunidade vivem apenas dentro dela: community_id NULL identifica o post
-- normal, que continua aparecendo no feed geral e no perfil do autor. Ver doc.md 10.5.
CREATE INDEX idx_posts_community_id
    ON posts(community_id);

CREATE INDEX idx_posts_community_id_status_created_at
    ON posts(community_id, status, created_at);
