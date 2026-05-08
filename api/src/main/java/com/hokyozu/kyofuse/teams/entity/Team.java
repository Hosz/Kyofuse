package com.hokyozu.kyofuse.teams.entity;

import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", length = 80, nullable = false)
    private String name;

    @Column(name = "slug", length = 100, nullable = false)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "region", length = 80)
    private String region;

    @Column(name = "min_premier_rating")
    private Integer minPremierRating;

    @Column(name = "max_premier_rating")
    private Integer maxPremierRating;

    @Column(name = "min_faceit_level")
    private Integer minFaceitLevel;

    @Column(name = "max_faceit_level")
    private Integer maxFaceitLevel;

    @Column(name = "min_gc_rank")
    private Integer minGcRank;

    @Column(name = "max_gc_rank")
    private Integer maxGcRank;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private TeamStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
