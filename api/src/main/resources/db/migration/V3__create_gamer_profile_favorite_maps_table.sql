CREATE TABLE gamer_profile_favorite_maps (
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,
    map_name VARCHAR(60) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_gamer_profile_favorite_maps_profile
     FOREIGN KEY (profile_id)
         REFERENCES gamer_profiles(id),

    CONSTRAINT uk_gamer_profile_favorite_maps_profile_map
     UNIQUE (profile_id, map_name)
);

CREATE INDEX idx_gamer_profile_favorite_maps_map_name
    ON gamer_profile_favorite_maps(map_name);