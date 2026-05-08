CREATE TABLE comments (
    id UUID PRIMARY KEY,

    post_id UUID NOT NULL,
    author_id UUID NOT NULL,

    content VARCHAR(2000) NOT NULL,
    status VARCHAR(40) NOT NULL,

    reaction_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_comments_post
      FOREIGN KEY (post_id)
          REFERENCES posts(id),

    CONSTRAINT fk_comments_author
      FOREIGN KEY (author_id)
          REFERENCES users(id),

    CONSTRAINT chk_comments_reaction_count
      CHECK (reaction_count >= 0),

    CONSTRAINT chk_comments_like_count
      CHECK (like_count >= 0)
);

CREATE INDEX idx_comments_post_id
    ON comments(post_id);

CREATE INDEX idx_comments_author_id
    ON comments(author_id);

CREATE INDEX idx_comments_status
    ON comments(status);

CREATE INDEX idx_comments_created_at
    ON comments(created_at);

CREATE INDEX idx_comments_post_status_created_at
    ON comments(post_id, status, created_at);