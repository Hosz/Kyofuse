CREATE TABLE gamer_profiles (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    nickname VARCHAR(40) NOT NULL,
    bio VARCHAR(500),
    avatar_url VARCHAR(500),

    country VARCHAR(80),
    state VARCHAR(80),
    city VARCHAR(80),

    main_role VARCHAR(40),
    secondary_role VARCHAR(40),

    premier_rating INTEGER,
    faceit_level INTEGER,
    gc_rank INTEGER,

    playstyle VARCHAR(40),

    looking_for_team BOOLEAN NOT NULL DEFAULT FALSE,
    looking_for_duo BOOLEAN NOT NULL DEFAULT FALSE,

    setup_status VARCHAR(30) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_gamer_profiles_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT uk_gamer_profiles_user_id
        UNIQUE (user_id),

    CONSTRAINT chk_gamer_profiles_premier_rating
        CHECK (
            premier_rating IS NULL
                OR premier_rating BETWEEN 1000 AND 40000
            ),

    CONSTRAINT chk_gamer_profiles_faceit_level
        CHECK (
            faceit_level IS NULL
                OR faceit_level BETWEEN 0 AND 10
            ),

    CONSTRAINT chk_gamer_profiles_gc_rank
        CHECK (
            gc_rank IS NULL
                OR gc_rank BETWEEN 0 AND 21
            )
);

CREATE INDEX idx_gamer_profiles_nickname
    ON gamer_profiles(nickname);

CREATE INDEX idx_gamer_profiles_main_role
    ON gamer_profiles(main_role);

CREATE INDEX idx_gamer_profiles_secondary_role
    ON gamer_profiles(secondary_role);

CREATE INDEX idx_gamer_profiles_premier_rating
    ON gamer_profiles(premier_rating);

CREATE INDEX idx_gamer_profiles_faceit_level
    ON gamer_profiles(faceit_level);

CREATE INDEX idx_gamer_profiles_gc_rank
    ON gamer_profiles(gc_rank);

CREATE INDEX idx_gamer_profiles_looking_for_team
    ON gamer_profiles(looking_for_team);

CREATE INDEX idx_gamer_profiles_looking_for_duo
    ON gamer_profiles(looking_for_duo);

CREATE INDEX idx_gamer_profiles_country_state
    ON gamer_profiles(country, state);