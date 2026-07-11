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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TeamMapperTest {

    @Test
    void toEntity_shouldMapTeamRequestToEntity() {
        User owner = createUser();
        TeamRequest request = new TeamRequest(
                "Test Team",
                "test-team",
                "Team description",
                "NA",
                1000,
                3000,
                5,
                10,
                "Gold",
                "Global Elite"
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
        assertThat(entity.getMinGcRank()).isEqualTo("Gold");
        assertThat(entity.getMaxGcRank()).isEqualTo("Global Elite");
        assertThat(entity.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    @Test
    void toEntity_shouldSetDefaultStatus() {
        User owner = createUser();
        TeamRequest request = new TeamRequest("Team", "team", null, null, null, null, null, null, null, null);

        Team entity = TeamMapper.toEntity(request, owner);

        assertThat(entity.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    @Test
    void toEntity_shouldSetCreatedAtAndUpdatedAt() {
        User owner = createUser();
        TeamRequest request = new TeamRequest("Team", "team", null, null, null, null, null, null, null, null);

        Instant beforeMapping = Instant.now();
        Team entity = TeamMapper.toEntity(request, owner);
        Instant afterMapping = Instant.now();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isBetween(beforeMapping, afterMapping);
    }

    @Test
    void toResponse_shouldMapEntityToResponse() {
        Team team = createTeam();
        TeamRequiredRole role1 = new TeamRequiredRole();
        role1.setRoleName(PlayerRole.RIFLER);
        TeamRequiredRole role2 = new TeamRequiredRole();
        role2.setRoleName(PlayerRole.AWP);
        List<TeamRequiredRole> roles = List.of(role1, role2);

        TeamResponse response = TeamMapper.toResponse(team, roles);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(team.getId());
        assertThat(response.ownerUsername()).isEqualTo(team.getOwner().getUsername());
        assertThat(response.name()).isEqualTo(team.getName());
        assertThat(response.slug()).isEqualTo(team.getSlug());
        assertThat(response.roles()).containsExactly(PlayerRole.RIFLER, PlayerRole.AWP);
    }

    @Test
    void toResponse_shouldMapEmptyRoles() {
        Team team = createTeam();
        List<TeamRequiredRole> roles = List.of();

        TeamResponse response = TeamMapper.toResponse(team, roles);

        assertThat(response.roles()).isEmpty();
    }

    @Test
    void toUpdate_shouldUpdateName() {
        Team team = createTeam();
        String newName = "Updated Team Name";
        UpdateTeamRequest updateRequest = new UpdateTeamRequest(newName, null, null, null, null, null, null, null, null);

        TeamMapper.toUpdate(team, updateRequest);

        assertThat(team.getName()).isEqualTo(newName);
    }

    @Test
    void toUpdate_shouldUpdateDescription() {
        Team team = createTeam();
        String newDescription = "New description";
        UpdateTeamRequest updateRequest = new UpdateTeamRequest(null, newDescription, null, null, null, null, null, null, null);

        TeamMapper.toUpdate(team, updateRequest);

        assertThat(team.getDescription()).isEqualTo(newDescription);
    }

    @Test
    void toUpdate_shouldClearDescriptionWhenNull() {
        Team team = createTeam();
        team.setDescription("Old description");
        UpdateTeamRequest updateRequest = new UpdateTeamRequest(null, null, null, null, null, null, null, null, null);

        TeamMapper.toUpdate(team, updateRequest);

        assertThat(team.getDescription()).isNull();
    }

    @Test
    void toUpdate_shouldUpdateAllFields() {
        Team team = createTeam();
        UpdateTeamRequest updateRequest = new UpdateTeamRequest(
                "New Name",
                "New Description",
                "EU",
                1500,
                2500,
                6,
                9,
                "Silver",
                "Supreme"
        );

        TeamMapper.toUpdate(team, updateRequest);

        assertThat(team.getName()).isEqualTo("New Name");
        assertThat(team.getDescription()).isEqualTo("New Description");
        assertThat(team.getRegion()).isEqualTo("EU");
        assertThat(team.getMinPremierRating()).isEqualTo(1500);
        assertThat(team.getMaxPremierRating()).isEqualTo(2500);
        assertThat(team.getMinFaceitLevel()).isEqualTo(6);
        assertThat(team.getMaxFaceitLevel()).isEqualTo(9);
        assertThat(team.getMinGcRank()).isEqualTo("Silver");
        assertThat(team.getMaxGcRank()).isEqualTo("Supreme");
    }

    @Test
    void toUpdate_shouldUpdateTimestamp() {
        Team team = createTeam();
        Instant originalUpdatedAt = team.getUpdatedAt();
        UpdateTeamRequest updateRequest = new UpdateTeamRequest("Updated", null, null, null, null, null, null, null, null);

        TeamMapper.toUpdate(team, updateRequest);

        assertThat(team.getUpdatedAt()).isNotNull();
        assertThat(team.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void toUpdate_shouldNotUpdateFieldsNotInRequest() {
        Team team = createTeam();
        String originalName = team.getName();
        String originalDescription = team.getDescription();
        UpdateTeamRequest updateRequest = new UpdateTeamRequest(null, null, null, null, null, null, null, null, null);

        TeamMapper.toUpdate(team, updateRequest);

        assertThat(team.getName()).isEqualTo(originalName);
        assertThat(team.getDescription()).isEqualTo(originalDescription);
    }

    private Team createTeam() {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setOwner(createUser());
        team.setName("Test Team");
        team.setSlug("test-team");
        team.setDescription("Test Description");
        team.setRegion("NA");
        team.setStatus(TeamStatus.ACTIVE);
        team.setCreatedAt(Instant.now());
        team.setUpdatedAt(Instant.now());
        return team;
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
