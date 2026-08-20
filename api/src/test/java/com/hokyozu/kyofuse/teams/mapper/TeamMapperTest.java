package com.hokyozu.kyofuse.teams.mapper;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMapperTest {

    @Test
    void canInstantiateMapper() {
        assertThat(new TeamMapper()).isNotNull();
    }

    @Test
    void toEntity_shouldMapTeamRequestToEntity() {
        User owner = createUser();
        List<PlayerRole> roles = new ArrayList<>();
        roles.add(PlayerRole.AWPER);
        
        TeamRequest request = new TeamRequest(
                "Test Team",
                "avatar.png",
                "banner.png",
                "test-team",
                "Team description",
                "NA",
                1000,
                3000,
                5,
                10,
                1,
                15,
                roles
        );

        Team entity = TeamMapper.toEntity(request, owner);

        assertThat(entity).isNotNull();
        assertThat(entity.getOwner()).isEqualTo(owner);
        assertThat(entity.getName()).isEqualTo("Test Team");
        assertThat(entity.getSlug()).isEqualTo("test-team");
        assertThat(entity.getDescription()).isEqualTo("Team description");
        assertThat(entity.getRegion()).isEqualTo("NA");
        assertThat(entity.getMinPremierRating()).isEqualTo(1000);
        assertThat(entity.getMaxPremierRating()).isEqualTo(3000);
        assertThat(entity.getMinFaceitLevel()).isEqualTo(5);
        assertThat(entity.getMaxFaceitLevel()).isEqualTo(10);
        assertThat(entity.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    @Test
    void toEntity_shouldSetDefaultStatus() {
        User owner = createUser();
        TeamRequest request = new TeamRequest("Team", null, null, "team", null, null, null, null, null, null, null, null, new ArrayList<>());

        Team entity = TeamMapper.toEntity(request, owner);

        assertThat(entity.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    @Test
    void toEntity_shouldSetCreatedAtAndUpdatedAt() {
        User owner = createUser();
        TeamRequest request = new TeamRequest("Team", null, null, "team", null, null, null, null, null, null, null, null, new ArrayList<>());

        Instant beforeMapping = Instant.now();
        Team entity = TeamMapper.toEntity(request, owner);
        Instant afterMapping = Instant.now();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isAfterOrEqualTo(beforeMapping);
        assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(afterMapping);
    }

    @Test
    void toResponse_shouldMapEntityToResponse() {
        Team team = createTeam();
        TeamRequiredRole role1 = new TeamRequiredRole();
        role1.setRoleName(PlayerRole.AWPER);
        TeamRequiredRole role2 = new TeamRequiredRole();
        role2.setRoleName(PlayerRole.SUPPORT);
        List<TeamRequiredRole> roles = List.of(role1, role2);

        TeamResponse response = TeamMapper.toResponse(team, roles);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(team.getId());
        assertThat(response.name()).isEqualTo(team.getName());
        assertThat(response.slug()).isEqualTo(team.getSlug());
    }

    @Test
    void toResponse_shouldMapEmptyRoles() {
        Team team = createTeam();
        List<TeamRequiredRole> roles = new ArrayList<>();

        TeamResponse response = TeamMapper.toResponse(team, roles);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(team.getId());
    }

    @Test
    void toUpdate_shouldUpdateTeamName() {
        Team team = createTeam();
        UpdateTeamRequest request = new UpdateTeamRequest(
                "Updated Team",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        TeamMapper.toUpdate(team, request);

        assertThat(team.getName()).isEqualTo("Updated Team");
    }

    @Test
    void toUpdate_shouldUpdateTeamDescription() {
        Team team = createTeam();
        UpdateTeamRequest request = new UpdateTeamRequest(
                null,
                "Updated description",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        TeamMapper.toUpdate(team, request);

        assertThat(team.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void toUpdate_shouldUpdateTeamAvatarAndBannerUrls() {
        Team team = createTeam();
        UpdateTeamRequest request = new UpdateTeamRequest(
                null,
                null,
                "https://example.com/avatar.png",
                "https://example.com/banner.png",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        TeamMapper.toUpdate(team, request);

        assertThat(team.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(team.getBannerUrl()).isEqualTo("https://example.com/banner.png");
    }

    @Test
    void toUpdate_shouldUpdateTeamRegion() {
        Team team = createTeam();
        UpdateTeamRequest request = new UpdateTeamRequest(
                null,
                null,
                null,
                null,
                "EU",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        TeamMapper.toUpdate(team, request);

        assertThat(team.getRegion()).isEqualTo("EU");
    }

    @Test
    void toUpdate_shouldNotUpdateTeamStatus() {
        Team team = createTeam();
        UpdateTeamRequest request = new UpdateTeamRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TeamStatus.CLOSED
        );

        TeamMapper.toUpdate(team, request);

        assertThat(team.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    private Team createTeam() {
        return Team.builder()
                .id(UUID.randomUUID())
                .owner(createUser())
                .name("Test Team")
                .slug("test-team")
                .description("Test description")
                .region("NA")
                .status(TeamStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("teamowner")
                .email("owner@example.com")
                .firstName("Team")
                .lastName("Owner")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
