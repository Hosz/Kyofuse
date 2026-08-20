package com.hokyozu.kyofuse.communities.entity;

import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "communities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "name", length = 80, nullable = false)
    private String name;

    @Column(name = "slug", length = 100, nullable = false)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20, nullable = false)
    private CommunityVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CommunityStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
