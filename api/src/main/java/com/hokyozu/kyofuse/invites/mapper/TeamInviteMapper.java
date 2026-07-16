package com.hokyozu.kyofuse.invites.mapper;

import com.hokyozu.kyofuse.invites.dto.request.TeamInviteRequest;
import com.hokyozu.kyofuse.invites.dto.response.TeamInviteResponse;
import com.hokyozu.kyofuse.invites.entity.TeamInvite;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class TeamInviteMapper {
    public static TeamInvite toEntity(Team team, User user, User receiver, TeamInviteRequest request) {
        TeamMemberType proposedMemberType = request.proposedMemberType();

        if (request.message() != null && request.message().isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }
        if (request.proposedMemberType() == null) {
            proposedMemberType = TeamMemberType.UNASSIGNED;
        }

        return TeamInvite.builder()
                .team(team)
                .sender(user)
                .receiver(receiver)
                .status(TeamInviteStatus.PENDING)
                .message(request.message())
                .proposedMemberType(proposedMemberType)
                .proposedRoleInTeam(request.proposedRoleInTeam())
                .canceledBy(null)
                .cancellationReason(null)
                .createdAt(Instant.now())
                .respondedAt(null)
                .canceledAt(null)
                .build();
    }

    public static TeamInviteResponse toResponse(TeamInvite teamInvite) {
        return new TeamInviteResponse(
                teamInvite.getTeam().getId(),
                teamInvite.getTeam().getName(),
                teamInvite.getSender().getId(),
                teamInvite.getSender().getUsername(),
                teamInvite.getReceiver().getId(),
                teamInvite.getReceiver().getUsername(),
                teamInvite.getStatus(),
                teamInvite.getMessage(),
                teamInvite.getProposedMemberType(),
                teamInvite.getProposedRoleInTeam(),
                teamInvite.getCreatedAt()
        );
    }
}
