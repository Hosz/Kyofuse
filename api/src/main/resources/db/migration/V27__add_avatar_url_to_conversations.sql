ALTER TABLE conversations
    ADD COLUMN avatar_url VARCHAR(500);

-- Só conversas GROUP têm foto própria: DIRECT exibe o avatar do outro participante e
-- COMMUNITY exibe o avatar da comunidade vinculada, então nesses dois casos a coluna
-- permanece NULL — mesma ideia das demais colunas condicionais por tipo desta tabela
-- (ver chk_conversations_type_consistency).
ALTER TABLE conversations
    ADD CONSTRAINT chk_conversations_avatar_only_for_group
        CHECK (avatar_url IS NULL OR type = 'GROUP');
