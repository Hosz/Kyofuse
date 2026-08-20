-- Times criados antes de o dono virar membro automaticamente ficaram sem a associação:
-- o dono não aparecia na lista de membros nem via o próprio time em "meus times".
-- MANAGER e assignment_due_at nulo de propósito: UNASSIGNED com prazo faria a rotina de
-- limpeza remover o dono do próprio time.
INSERT INTO team_members (
    id, team_id, user_id, role_in_team, member_type, status,
    joined_at, left_at, assignment_due_at, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    t.id,
    t.owner_id,
    NULL,
    'MANAGER',
    'ACTIVE',
    t.created_at,
    NULL,
    NULL,
    t.created_at,
    t.created_at
FROM teams t
WHERE NOT EXISTS (
    SELECT 1 FROM team_members tm
    WHERE tm.team_id = t.id AND tm.user_id = t.owner_id
);
