CREATE TABLE teams (
    id UUID PRIMARY KEY,

    owner_id UUID NOT NULL,

    name VARCHAR(80) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    region VARCHAR(80),

    min_premier_rating INTEGER,
    max_premier_rating INTEGER,

    min_faceit_level INTEGER,
    max_faceit_level INTEGER,

    min_gc_rank INTEGER,
    max_gc_rank INTEGER,

    status VARCHAR(40) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_teams_owner
       FOREIGN KEY (owner_id)
           REFERENCES users(id),

    CONSTRAINT uk_teams_slug
       UNIQUE (slug),

    CONSTRAINT chk_teams_min_premier_rating
       CHECK (
           min_premier_rating IS NULL
               OR min_premier_rating BETWEEN 1000 AND 40000
           ),

    CONSTRAINT chk_teams_max_premier_rating
       CHECK (
           max_premier_rating IS NULL
               OR max_premier_rating BETWEEN 1000 AND 40000
           ),

    CONSTRAINT chk_teams_premier_rating_range
       CHECK (
           min_premier_rating IS NULL
               OR max_premier_rating IS NULL
               OR min_premier_rating <= max_premier_rating
           ),

    CONSTRAINT chk_teams_min_faceit_level
       CHECK (
           min_faceit_level IS NULL
               OR min_faceit_level BETWEEN 0 AND 10
           ),

    CONSTRAINT chk_teams_max_faceit_level
       CHECK (
           max_faceit_level IS NULL
               OR max_faceit_level BETWEEN 0 AND 10
           ),

    CONSTRAINT chk_teams_faceit_level_range
       CHECK (
           min_faceit_level IS NULL
               OR max_faceit_level IS NULL
               OR min_faceit_level <= max_faceit_level
           ),

    CONSTRAINT chk_teams_min_gc_rank
       CHECK (
           min_gc_rank IS NULL
               OR min_gc_rank BETWEEN 0 AND 21
           ),

    CONSTRAINT chk_teams_max_gc_rank
       CHECK (
           max_gc_rank IS NULL
               OR max_gc_rank BETWEEN 0 AND 21
           ),

    CONSTRAINT chk_teams_gc_rank_range
       CHECK (
           min_gc_rank IS NULL
               OR max_gc_rank IS NULL
               OR min_gc_rank <= max_gc_rank
           )
);

CREATE INDEX idx_teams_owner_id
    ON teams(owner_id);

CREATE INDEX idx_teams_status
    ON teams(status);

CREATE INDEX idx_teams_region
    ON teams(region);

CREATE INDEX idx_teams_min_premier_rating
    ON teams(min_premier_rating);

CREATE INDEX idx_teams_max_premier_rating
    ON teams(max_premier_rating);

CREATE INDEX idx_teams_min_faceit_level
    ON teams(min_faceit_level);

CREATE INDEX idx_teams_max_faceit_level
    ON teams(max_faceit_level);

CREATE INDEX idx_teams_min_gc_rank
    ON teams(min_gc_rank);

CREATE INDEX idx_teams_max_gc_rank
    ON teams(max_gc_rank);