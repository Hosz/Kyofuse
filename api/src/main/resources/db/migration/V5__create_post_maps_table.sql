CREATE TABLE post_maps (
    id UUID PRIMARY KEY,

    post_id UUID NOT NULL,
    map_name VARCHAR(60) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_post_maps_post
       FOREIGN KEY (post_id)
           REFERENCES posts(id),

    CONSTRAINT uk_post_maps_post_map
       UNIQUE (post_id, map_name)
);

CREATE INDEX idx_post_maps_map_name
    ON post_maps(map_name);