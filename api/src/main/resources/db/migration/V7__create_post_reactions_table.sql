CREATE TABLE post_reactions (
    id UUID PRIMARY KEY,

    post_id UUID NOT NULL,
    user_id UUID NOT NULL,

    reaction_type VARCHAR(40) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_post_reactions_post
        FOREIGN KEY (post_id)
            REFERENCES posts(id),

    CONSTRAINT fk_post_reactions_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT uk_post_reactions_post_user
        UNIQUE (post_id, user_id)
);

CREATE INDEX idx_post_reactions_post_id
    ON post_reactions(post_id);

CREATE INDEX idx_post_reactions_user_id
    ON post_reactions(user_id);

CREATE INDEX idx_post_reactions_reaction_type
    ON post_reactions(reaction_type);