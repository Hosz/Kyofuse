CREATE TABLE conversations (
    id UUID PRIMARY KEY,

    type VARCHAR(20) NOT NULL,

    name VARCHAR(80),
    created_by UUID NOT NULL,

    community_id UUID,

    direct_user_one_id UUID,
    direct_user_two_id UUID,
    direct_message_status VARCHAR(20),

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_conversations_created_by
        FOREIGN KEY (created_by)
            REFERENCES users(id),

    CONSTRAINT fk_conversations_community
        FOREIGN KEY (community_id)
            REFERENCES communities(id),

    CONSTRAINT fk_conversations_direct_user_one
        FOREIGN KEY (direct_user_one_id)
            REFERENCES users(id),

    CONSTRAINT fk_conversations_direct_user_two
        FOREIGN KEY (direct_user_two_id)
            REFERENCES users(id),

    CONSTRAINT uk_conversations_community
        UNIQUE (community_id),

    CONSTRAINT uk_conversations_direct_pair
        UNIQUE (direct_user_one_id, direct_user_two_id),

    -- Cada tipo de conversa exige exatamente o seu próprio conjunto de colunas
    -- preenchido; os demais permanecem NULL.
    CONSTRAINT chk_conversations_type_consistency
        CHECK (
            (type = 'DIRECT'
                AND direct_user_one_id IS NOT NULL
                AND direct_user_two_id IS NOT NULL
                AND direct_message_status IS NOT NULL
                AND community_id IS NULL)
            OR (type = 'GROUP'
                AND direct_user_one_id IS NULL
                AND direct_user_two_id IS NULL
                AND direct_message_status IS NULL
                AND community_id IS NULL)
            OR (type = 'COMMUNITY'
                AND direct_user_one_id IS NULL
                AND direct_user_two_id IS NULL
                AND direct_message_status IS NULL
                AND community_id IS NOT NULL)
        ),

    -- direct_user_one_id sempre guarda o menor UUID (ordem lexicográfica) entre os dois
    -- participantes, e direct_user_two_id o maior. Isso garante, junto da constraint
    -- uk_conversations_direct_pair, no máximo uma conversa DIRECT por par de usuários,
    -- independentemente de quem iniciou a conversa.
    CONSTRAINT chk_conversations_direct_users_order
        CHECK (
            direct_user_one_id IS NULL
                OR direct_user_two_id IS NULL
                OR direct_user_one_id < direct_user_two_id
        )
);

CREATE INDEX idx_conversations_type
    ON conversations(type);

CREATE INDEX idx_conversations_created_by
    ON conversations(created_by);

CREATE INDEX idx_conversations_community_id
    ON conversations(community_id);

CREATE INDEX idx_conversations_direct_user_one_id
    ON conversations(direct_user_one_id);

CREATE INDEX idx_conversations_direct_user_two_id
    ON conversations(direct_user_two_id);

CREATE INDEX idx_conversations_direct_message_status
    ON conversations(direct_message_status);

CREATE INDEX idx_conversations_created_at
    ON conversations(created_at);
