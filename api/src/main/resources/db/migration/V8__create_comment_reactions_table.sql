CREATE TABLE comment_reactions (
    id UUID PRIMARY KEY,

    comment_id UUID NOT NULL,
    user_id UUID NOT NULL,

    reaction_type VARCHAR(40) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_comment_reactions_comment
       FOREIGN KEY (comment_id)
           REFERENCES comments(id),

    CONSTRAINT fk_comment_reactions_user
       FOREIGN KEY (user_id)
           REFERENCES users(id),

    CONSTRAINT uk_comment_reactions_comment_user
       UNIQUE (comment_id, user_id)
);

CREATE INDEX idx_comment_reactions_comment_id
    ON comment_reactions(comment_id);

CREATE INDEX idx_comment_reactions_user_id
    ON comment_reactions(user_id);

CREATE INDEX idx_comment_reactions_reaction_type
    ON comment_reactions(reaction_type);