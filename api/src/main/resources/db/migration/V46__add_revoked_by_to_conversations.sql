ALTER TABLE conversations
    ADD COLUMN revoked_by UUID;

ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_revoked_by
        FOREIGN KEY (revoked_by)
            REFERENCES users(id);

ALTER TABLE conversations
    ADD CONSTRAINT chk_conversations_revoked_by_only_for_direct
        CHECK (revoked_by IS NULL OR type = 'DIRECT');

CREATE INDEX idx_conversations_revoked_by
    ON conversations(revoked_by);
