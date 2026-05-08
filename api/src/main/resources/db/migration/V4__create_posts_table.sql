CREATE TABLE posts (
    id UUID PRIMARY KEY,

    author_id UUID NOT NULL,

    content VARCHAR(2000) NOT NULL,
    post_type VARCHAR(40) NOT NULL,
    visibility VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,

    reaction_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_posts_author
       FOREIGN KEY (author_id)
           REFERENCES users(id),

    CONSTRAINT chk_posts_reaction_count
       CHECK (reaction_count >= 0),

    CONSTRAINT chk_posts_like_count
       CHECK (like_count >= 0),

    CONSTRAINT chk_posts_comment_count
       CHECK (comment_count >= 0)
);

CREATE INDEX idx_posts_author_id
    ON posts(author_id);

CREATE INDEX idx_posts_post_type
    ON posts(post_type);

CREATE INDEX idx_posts_visibility
    ON posts(visibility);

CREATE INDEX idx_posts_status
    ON posts(status);

CREATE INDEX idx_posts_created_at
    ON posts(created_at);

CREATE INDEX idx_posts_status_visibility_created_at
    ON posts(status, visibility, created_at);