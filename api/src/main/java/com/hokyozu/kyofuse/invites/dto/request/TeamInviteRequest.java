package com.hokyozu.kyofuse.invites.dto.request;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;

public record TeamInviteRequest(
        String message,
        TeamMemberType proposedMemberType,
        PlayerRole proposedRoleInTeam
) {
}
