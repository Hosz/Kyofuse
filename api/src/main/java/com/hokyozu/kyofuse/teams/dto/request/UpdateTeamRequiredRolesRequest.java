package com.hokyozu.kyofuse.teams.dto.request;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateTeamRequiredRolesRequest(
        @NotNull
        List<@NotNull PlayerRole> requiredRoles
) {
}
