package com.hokyozu.kyofuse.profiles.entity;

import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(length = 40, nullable = false)
    private String nickname;

    @Column(length = 500)
    private String bio;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 80)
    private String country;

    @Column(length = 80)
    private String state;

    @Column(length = 80)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PlayerRole mainRole;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PlayerRole secondaryRole;

    private Integer premierRating;

    private Integer faceitLevel;

    private Integer gcRank;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private Playstyle playstyle;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lookingForTeam = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lookingForDuo = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private GamerProfileSetupStatus setupStatus;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
