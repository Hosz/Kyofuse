ALTER TABLE user_pinned_communities
    ADD COLUMN position INTEGER;

-- Preserva a ordem atual (data de fixação) como posição inicial, para que a barra de
-- abas do feed continue exatamente como o usuário já a enxerga hoje.
WITH ordered AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at) - 1 AS row_position
    FROM user_pinned_communities
)
UPDATE user_pinned_communities upc
SET position = ordered.row_position
FROM ordered
WHERE upc.id = ordered.id;

ALTER TABLE user_pinned_communities
    ALTER COLUMN position SET NOT NULL;

CREATE INDEX idx_user_pinned_communities_user_id_position
    ON user_pinned_communities(user_id, position);
