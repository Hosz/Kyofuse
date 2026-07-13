package com.hokyozu.kyofuse.teams.dto.response;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;

import java.time.Instant;

public record TeamMemberResponse(
        String teamName,
        String userName,
        PlayerRole roleInTeam,
        TeamMemberType memberType,
        TeamMemberStatus status,
        Instant joinedAt,
        Instant assignmentDueAt,
        Instant createdAt,
        Instant updatedAt
) {
}
