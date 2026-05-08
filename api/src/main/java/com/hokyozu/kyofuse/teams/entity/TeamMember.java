package com.hokyozu.kyofuse.teams.entity;

import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team teamId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(length = 40)
    private String roleIntTeam;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private TeamMemberType memberType;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private TeamMemberStatus status;

    @Column(nullable = false)
    private OffsetDateTime joinedAt;

    private OffsetDateTime leftAt;

    private OffsetDateTime assignmentDueAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
