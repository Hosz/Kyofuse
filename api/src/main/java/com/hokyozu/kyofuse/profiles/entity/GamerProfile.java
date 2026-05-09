package com.hokyozu.kyofuse.profiles.entity;

import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gamer_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nickname", length = 40, nullable = false)
    private String nickname;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "country", length = 80)
    private String country;

    @Column(name = "state", length = 80)
    private String state;

    @Column(name = "city", length = 80)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "main_role", length = 40)
    private PlayerRole mainRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "secondary_role", length = 40)
    private PlayerRole secondaryRole;

    @Column(name = "premier_rating")
    private Integer premierRating;

    @Column(name = "faceit_level")
    private Integer faceitLevel;

    @Column(name = "gc_rank")
    private Integer gcRank;

    @Enumerated(EnumType.STRING)
    @Column(name = "playstyle", length = 40)
    private Playstyle playstyle;

    @Column(name = "looking_for_team", nullable = false)
    @Builder.Default
    private Boolean lookingForTeam = false;

    @Column(name = "looking_for_duo", nullable = false)
    @Builder.Default
    private Boolean lookingForDuo = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "setup_status", length = 30, nullable = false)
    private GamerProfileSetupStatus setupStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
