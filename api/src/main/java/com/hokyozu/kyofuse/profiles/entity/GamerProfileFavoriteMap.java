package com.hokyozu.kyofuse.profiles.entity;

import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private User profileId;

    @Column(length = 60, nullable = false)
    private String mapName;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
