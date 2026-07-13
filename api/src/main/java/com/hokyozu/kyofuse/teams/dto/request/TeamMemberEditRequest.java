package com.hokyozu.kyofuse.teams.dto.request;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;

public record TeamMemberEditRequest(
        PlayerRole roleInTeam,
        TeamMemberType memberType
) {
}
