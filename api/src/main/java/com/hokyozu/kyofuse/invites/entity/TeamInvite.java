package com.hokyozu.kyofuse.invites.entity;

import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_invites")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team teamId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User senderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiverId;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private TeamInviteStatus status;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private TeamMemberType proposedMemberType;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PlayerRole proposedRoleInTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by")
    private User canceledBy;

    @Column(length = 500)
    private String cancellationReason;

    @Column(nullable = false)
    private OffsetDateTime createAt;

    private OffsetDateTime respondedAt;

    private OffsetDateTime canceledAt;
}
