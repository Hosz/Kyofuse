package com.hokyozu.kyofuse.profiles.entity;

import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "gamer_profile_favorite_maps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamerProfileFavoriteMap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private GamerProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "map_name", length = 60, nullable = false)
    private Cs2Map mapName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
