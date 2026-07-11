package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private TeamFinder teamFinder;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamRequiredRoleRepository teamRequiredRoleRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void createTeamsCreatesActiveTeamAndDeduplicatesRequiredRoles() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("owner")
                .status(UserStatus.ACTIVE)
                .build();
        TeamRequest request = validRequest(List.of(PlayerRole.AWPER, PlayerRole.AWPER, PlayerRole.RIFLER));

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(teamId);
            return team;
        });
        when(teamRequiredRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.createTeams(request, userId);

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        verify(teamRequiredRoleRepository).saveAll(anyList());

        Team savedTeam = teamCaptor.getValue();
        assertThat(savedTeam.getOwner()).isSameAs(user);
        assertThat(savedTeam.getName()).isEqualTo("Kyofuse Academy");
        assertThat(savedTeam.getSlug()).isEqualTo("kyofuse-academy");
        assertThat(savedTeam.getStatus()).isEqualTo(TeamStatus.ACTIVE);
        assertThat(response.id()).isEqualTo(teamId);
        assertThat(response.ownerName()).isEqualTo("owner");
        assertThat(response.requiredRoles()).containsExactly(PlayerRole.AWPER, PlayerRole.RIFLER);
    }

    @Test
    void createTeamsSavesNoRequiredRolesWhenRequestRolesIsNull() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("owner")
                .status(UserStatus.ACTIVE)
                .build();
        TeamRequest request = validRequest(null);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(teamRequiredRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.createTeams(request, userId);

        ArgumentCaptor<List<TeamRequiredRole>> rolesCaptor = rolesCaptor();
        verify(teamRequiredRoleRepository).saveAll(rolesCaptor.capture());
        assertThat(rolesCaptor.getValue()).isEmpty();
        assertThat(response.requiredRoles()).isEmpty();
    }

    @Test
    void createTeamsRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVE)
                .build();
        TeamRequest request = validRequest(List.of(PlayerRole.AWPER));

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> teamService.createTeams(request, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User must be active to create a team.");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void createTeamsRejectsDuplicatedSlug() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        TeamRequest request = validRequest(List.of(PlayerRole.AWPER));

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.existsBySlug("kyofuse-academy")).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeams(request, userId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Slug já está em uso.");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void editTeamUpdatesAllowedFieldsForActiveOwner() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = User.builder()
                .id(userId)
                .username("owner")
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);
        UpdateTeamRequest request = validUpdateRequest();

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.editTeam(userId, teamId, request);

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());

        Team savedTeam = teamCaptor.getValue();
        assertThat(savedTeam.getName()).isEqualTo("Updated Academy");
        assertThat(savedTeam.getDescription()).isEqualTo("Updated description");
        assertThat(savedTeam.getRegion()).isEqualTo("NA");
        assertThat(savedTeam.getMinPremierRating()).isEqualTo(12000);
        assertThat(savedTeam.getMaxPremierRating()).isEqualTo(25000);
        assertThat(savedTeam.getMinFaceitLevel()).isEqualTo(4);
        assertThat(savedTeam.getMaxFaceitLevel()).isEqualTo(9);
        assertThat(savedTeam.getMinGcRank()).isEqualTo(5);
        assertThat(savedTeam.getMaxGcRank()).isEqualTo(18);
        assertThat(savedTeam.getSlug()).isEqualTo("kyofuse-academy");
        assertThat(savedTeam.getOwner()).isSameAs(owner);
        assertThat(savedTeam.getStatus()).isEqualTo(TeamStatus.ACTIVE);
        assertThat(savedTeam.getUpdatedAt()).isNotNull();
        assertThat(response.name()).isEqualTo("Updated Academy");
    }

    @Test
    void editTeamRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVE)
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, validUpdateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não está ativo.");

        verify(teamFinder, never()).findTeamById(any());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void editTeamRejectsInactiveTeam() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);
        team.setStatus(TeamStatus.INACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, validUpdateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Time não está ativo");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void editTeamRejectsNonOwner() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        User owner = User.builder()
                .id(UUID.randomUUID())
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, validUpdateRequest()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Apenas o dono do time pode fazer alterações");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void editTeamRejectsInactiveStatusInUpdateEndpoint() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);
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
                TeamStatus.INACTIVE
        );

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Use o endpoint de inativação para inativar o time.");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void editTeamRejectsInvalidFinalRangeAfterPartialUpdate() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);
        team.setMaxPremierRating(15000);
        UpdateTeamRequest request = new UpdateTeamRequest(
                null,
                null,
                null,
                20000,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("minPremierRating must be less than or equal to maxPremierRating");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void inactiveTeamSetsStatusToInactiveForActiveOwner() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = User.builder()
                .id(userId)
                .username("owner")
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.inactiveTeam(userId, teamId);

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());

        Team savedTeam = teamCaptor.getValue();
        assertThat(savedTeam.getStatus()).isEqualTo(TeamStatus.INACTIVE);
        assertThat(savedTeam.getUpdatedAt()).isNotNull();
        assertThat(response.status()).isEqualTo(TeamStatus.INACTIVE);
    }

    @Test
    void inactiveTeamRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVE)
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> teamService.inactiveTeam(userId, teamId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não está ativo.");

        verify(teamFinder, never()).findTeamById(any());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void inactiveTeamRejectsAlreadyInactiveTeam() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);
        team.setStatus(TeamStatus.INACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);

        assertThatThrownBy(() -> teamService.inactiveTeam(userId, teamId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Time já não está ativo.");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void inactiveTeamRejectsNonOwner() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();
        User owner = User.builder()
                .id(UUID.randomUUID())
                .status(UserStatus.ACTIVE)
                .build();
        Team team = activeTeam(teamId, owner);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);

        assertThatThrownBy(() -> teamService.inactiveTeam(userId, teamId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Apenas o dono do time pode fazer alterações");

        verify(teamRepository, never()).save(any());
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<TeamRequiredRole>> rolesCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    private TeamRequest validRequest(List<PlayerRole> requiredRoles) {
        return new TeamRequest(
                "Kyofuse Academy",
                "kyofuse-academy",
                "Development team",
                "BR",
                1000,
                40000,
                1,
                10,
                1,
                21,
                requiredRoles
        );
    }

    private UpdateTeamRequest validUpdateRequest() {
        return new UpdateTeamRequest(
                "Updated Academy",
                "Updated description",
                "NA",
                12000,
                25000,
                4,
                9,
                5,
                18,
                TeamStatus.ACTIVE
        );
    }

    private Team activeTeam(UUID teamId, User owner) {
        return Team.builder()
                .id(teamId)
                .owner(owner)
                .name("Kyofuse Academy")
                .slug("kyofuse-academy")
                .description("Development team")
                .region("BR")
                .minPremierRating(1000)
                .maxPremierRating(30000)
                .minFaceitLevel(1)
                .maxFaceitLevel(10)
                .minGcRank(1)
                .maxGcRank(21)
                .status(TeamStatus.ACTIVE)
                .build();
    }
}
