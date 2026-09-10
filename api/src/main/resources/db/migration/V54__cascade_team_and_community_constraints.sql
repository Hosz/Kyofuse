-- Ajusta constraints para exclusao em cascata ou desvinculacao (SET NULL)
-- permitindo delecao limpa de times e comunidades sem violar integridade referencial.

ALTER TABLE team_required_roles
    DROP CONSTRAINT fk_team_required_roles_team,
    ADD CONSTRAINT fk_team_required_roles_team
        FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE;

ALTER TABLE team_members
    DROP CONSTRAINT fk_team_members_team,
    ADD CONSTRAINT fk_team_members_team
        FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE;

ALTER TABLE team_invites
    DROP CONSTRAINT fk_team_invites_team,
    ADD CONSTRAINT fk_team_invites_team
        FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE;

ALTER TABLE communities
    DROP CONSTRAINT fk_communities_team,
    ADD CONSTRAINT fk_communities_team
        FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL;

ALTER TABLE community_members
    DROP CONSTRAINT fk_community_members_community,
    ADD CONSTRAINT fk_community_members_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE;

ALTER TABLE user_pinned_communities
    DROP CONSTRAINT fk_user_pinned_communities_community,
    ADD CONSTRAINT fk_user_pinned_communities_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE;

ALTER TABLE posts
    DROP CONSTRAINT fk_posts_community,
    ADD CONSTRAINT fk_posts_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL;

ALTER TABLE conversations
    DROP CONSTRAINT fk_conversations_community,
    ADD CONSTRAINT fk_conversations_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE SET NULL;
