package com.hokyozu.kyofuse.teams.entity;

import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User ownerId;

    @Column(length = 80, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(length = 80)
    private String region;

    private Integer minPremierRating;

    private Integer maxPremierRating;

    private Integer minFaceitLevel;

    private Integer maxFaceitLevel;

    private Integer minGcRank;

    private Integer maxGcRank;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private TeamStatus status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
