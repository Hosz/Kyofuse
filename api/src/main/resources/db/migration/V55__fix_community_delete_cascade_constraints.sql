-- Corrige constraints de conversas, mensagens e solicitacoes para exclusao em cascata ao apagar comunidade.
-- A definicao anterior (ON DELETE SET NULL em conversations) violava a constraint chk_conversations_type_consistency.

ALTER TABLE conversation_members
    DROP CONSTRAINT fk_conversation_members_conversation,
    ADD CONSTRAINT fk_conversation_members_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

ALTER TABLE messages
    DROP CONSTRAINT fk_messages_conversation,
    ADD CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE;

ALTER TABLE conversations
    DROP CONSTRAINT fk_conversations_community,
    ADD CONSTRAINT fk_conversations_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE;

ALTER TABLE community_join_requests
    DROP CONSTRAINT fk_community_join_requests_community,
    ADD CONSTRAINT fk_community_join_requests_community
        FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE;
