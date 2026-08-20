-- Comunidades criadas antes das garantias atuais ficaram sem dois registros que hoje
-- nascem junto delas: o vínculo de membro do próprio dono e a conversa COMMUNITY.
-- Sem eles o dono não aparece entre os membros e o chat da comunidade não abre.

-- 1. Vínculo ADMIN do dono, onde ainda não existir nenhum vínculo dele.
INSERT INTO community_members (id, community_id, user_id, role, status, joined_at, created_at, updated_at)
SELECT gen_random_uuid(), c.id, c.owner_id, 'ADMIN', 'ACTIVE', now(), now(), now()
FROM communities c
WHERE NOT EXISTS (
    SELECT 1
    FROM community_members cm
    WHERE cm.community_id = c.id
      AND cm.user_id = c.owner_id
);

-- 2. Conversa COMMUNITY das comunidades que ainda não têm uma (uk_conversations_community
--    já garante no máximo uma por comunidade).
INSERT INTO conversations (id, type, created_by, community_id, created_at, updated_at)
SELECT gen_random_uuid(), 'COMMUNITY', c.owner_id, c.id, now(), now()
FROM communities c
WHERE NOT EXISTS (
    SELECT 1
    FROM conversations conv
    WHERE conv.community_id = c.id
);
