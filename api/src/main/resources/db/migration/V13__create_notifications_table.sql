CREATE TABLE notifications (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,
    actor_id UUID,

    type VARCHAR(60) NOT NULL,
    title VARCHAR(160) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(40) NOT NULL,

    target_type VARCHAR(60),
    target_id UUID,

    metadata_json JSONB,

    created_at TIMESTAMPTZ NOT NULL,
    read_at TIMESTAMPTZ,

    CONSTRAINT fk_notifications_user
       FOREIGN KEY (user_id)
           REFERENCES users(id),

    CONSTRAINT fk_notifications_actor
       FOREIGN KEY (actor_id)
           REFERENCES users(id)
);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_actor_id
    ON notifications(actor_id);

CREATE INDEX idx_notifications_type
    ON notifications(type);

CREATE INDEX idx_notifications_status
    ON notifications(status);

CREATE INDEX idx_notifications_target_type
    ON notifications(target_type);

CREATE INDEX idx_notifications_target_id
    ON notifications(target_id);

CREATE INDEX idx_notifications_created_at
    ON notifications(created_at);

CREATE INDEX idx_notifications_read_at
    ON notifications(read_at);

CREATE INDEX idx_notifications_user_status_created_at
    ON notifications(user_id, status, created_at);