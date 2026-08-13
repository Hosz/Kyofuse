package com.hokyozu.kyofuse.invites.dto.response;

import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;

import java.time.Instant;
import java.util.UUID;

public record TeamInviteResponse(
        UUID id,
        UUID teamId,
        String teamName,
        UUID senderId,
        String senderName,
        UUID receiverId,
        String receiverName,
        TeamInviteStatus status,
        String message,
        TeamMemberType proposedMemberType,
        PlayerRole proposedRoleInTeam,
        Instant createdAt
) {
}
