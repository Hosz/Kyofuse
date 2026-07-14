package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamRequiredRoleMapperTest {

    @Test
    void canInstantiateMapper() {
        assertThat(new TeamRequiredRoleMapper()).isNotNull();
    }

    @Test
    void toEntityMapsTeamRoleAndCreatedAt() {
        Team team = Team.builder()
                .id(UUID.randomUUID())
                .build();
        Instant before = Instant.now();

        TeamRequiredRole role = TeamRequiredRoleMapper.toEntity(team, PlayerRole.AWPER);

        assertThat(role.getTeam()).isSameAs(team);
        assertThat(role.getRoleName()).isEqualTo(PlayerRole.AWPER);
        assertThat(role.getCreatedAt()).isBetween(before, Instant.now());
    }
}
