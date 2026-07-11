package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;

import java.time.Instant;

public class TeamRequiredRoleMapper {
    public static TeamRequiredRole toEntity(Team team, PlayerRole role) {
        return TeamRequiredRole.builder()
                .team(team)
                .roleName(role)
                .createdAt(Instant.now())
                .build();
    }
}
