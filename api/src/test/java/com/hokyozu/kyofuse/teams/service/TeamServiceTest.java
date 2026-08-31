package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.chat.service.ConversationService;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.service.CommunityService;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.teams.dto.request.TeamFilter;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequiredRolesRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private CommunityService communityService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private com.hokyozu.kyofuse.storage.service.ImageProcessingService imageProcessingService;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @Spy
    private TeamChecker teamChecker = new TeamChecker();

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private GamerProfileRepository gamerProfileRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void listPlayersLookingForTeamExcludesWhoIsAlreadyInTheTeam() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User owner = User.builder().id(userId).username("owner").status(UserStatus.ACTIVE).build();
        User member = User.builder().id(memberId).username("member").status(UserStatus.ACTIVE).build();
        Team team = Team.builder().id(teamId).owner(owner).status(TeamStatus.ACTIVE).build();
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamMemberRepository.findByTeam(team))
                .thenReturn(List.of(TeamMember.builder().team(team).user(member).build()));
        when(gamerProfileRepository.findByLookingForTeamTrueAndUser_StatusAndUserIdNotIn(
                eq(UserStatus.ACTIVE), anyList(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        teamService.listPlayersLookingForTeam(userId, teamId, pageable);

        ArgumentCaptor<List<UUID>> excludedCaptor = uuidListCaptor();
        verify(gamerProfileRepository).findByLookingForTeamTrueAndUser_StatusAndUserIdNotIn(
                eq(UserStatus.ACTIVE), excludedCaptor.capture(), eq(pageable));
        // Convidar quem já está no time seria recusado pelo backend do convite.
        assertThat(excludedCaptor.getValue()).contains(memberId, userId);
    }

    @Test
    void listPlayersLookingForTeamRejectsWhoIsNotTheOwner() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User stranger = User.builder().id(userId).username("stranger").status(UserStatus.ACTIVE).build();
        Team team = Team.builder().id(teamId).owner(User.builder().id(UUID.randomUUID()).build()).status(TeamStatus.ACTIVE).build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(stranger);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        doThrow(new BadRequestException("Usuário não é o dono do time."))
                .when(teamChecker).checkUserIsOwner(team, stranger);

        assertThatThrownBy(() -> teamService.listPlayersLookingForTeam(userId, teamId, PageRequest.of(0, 20)))
                .isInstanceOf(BadRequestException.class);

        verify(gamerProfileRepository, never())
                .findByLookingForTeamTrueAndUser_StatusAndUserIdNotIn(any(), anyList(), any());
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<UUID>> uuidListCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    @Test
    void createTeamsAddsTheOwnerAsAMemberSoTheTeamShowsUpInTheirList() {
        // "Meus times" e a lista de membros saem de team_members: sem essa linha o dono
        // não via o próprio time nem aparecia entre os membros.
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = User.builder().id(userId).username("owner").status(UserStatus.ACTIVE).build();
        Community community = Community.builder().id(UUID.randomUUID()).owner(user).build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(teamId);
            return team;
        });
        when(teamRequiredRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(communityService.autoCreateTeamCommunity(eq(user), any(Team.class))).thenReturn(community);

        teamService.createTeams(validRequest(List.of(PlayerRole.AWPER)), userId);

        ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
        verify(teamMemberRepository).save(memberCaptor.capture());

        TeamMember member = memberCaptor.getValue();
        assertThat(member.getUser()).isSameAs(user);
        assertThat(member.getTeam().getId()).isEqualTo(teamId);
        assertThat(member.getStatus()).isEqualTo(TeamMemberStatus.ACTIVE);
        // MANAGER e sem prazo: UNASSIGNED com assignmentDueAt faria a rotina de limpeza
        // remover o próprio dono do time depois de 14 dias.
        assertThat(member.getMemberType()).isEqualTo(TeamMemberType.MANAGER);
        assertThat(member.getAssignmentDueAt()).isNull();
    }

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

        Community community = Community.builder().id(UUID.randomUUID()).owner(user).build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(teamId);
            return team;
        });
        when(teamRequiredRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(communityService.autoCreateTeamCommunity(eq(user), any(Team.class))).thenReturn(community);

        TeamResponse response = teamService.createTeams(request, userId);

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        verify(teamRequiredRoleRepository).saveAll(anyList());
        verify(communityService).autoCreateTeamCommunity(user, teamCaptor.getValue());
        verify(conversationService).createCommunityConversation(community, user);

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
        Community community = Community.builder().id(UUID.randomUUID()).owner(user).build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(teamRequiredRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(communityService.autoCreateTeamCommunity(eq(user), any(Team.class))).thenReturn(community);

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
                .hasMessage("Usuário não ativo.");

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
    void detailTeamReturnsTeamWithRequiredRoles() {
        UUID teamId = UUID.randomUUID();
        User owner = activeUser(UUID.randomUUID(), "owner");
        Team team = activeTeam(teamId, owner);
        TeamRequiredRole role = requiredRole(team, PlayerRole.AWPER);

        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of(role));

        TeamResponse response = teamService.detailTeam(teamId);

        assertThat(response.id()).isEqualTo(teamId);
        assertThat(response.requiredRoles()).containsExactly(PlayerRole.AWPER);
    }

    @Test
    void detailTeamRejectsInactiveTeam() {
        UUID teamId = UUID.randomUUID();
        Team team = activeTeam(teamId, activeUser(UUID.randomUUID(), "owner"));
        team.setStatus(TeamStatus.INACTIVE);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);

        assertThatThrownBy(() -> teamService.detailTeam(teamId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Time inativo.");

        verify(teamRequiredRoleRepository, never()).findByTeamId(any());
    }

    @Test
    void listingTeamsMapsRequiredRolesByTeam() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Team first = activeTeam(UUID.randomUUID(), owner);
        Team second = activeTeam(UUID.randomUUID(), owner);
        second.setName("Second Team");
        PageRequest pageable = PageRequest.of(0, 10);
        TeamFilter filter = new TeamFilter(
                "team",
                "kyofuse-academy",
                TeamStatus.ACTIVE,
                "BR",
                List.of(PlayerRole.AWPER),
                1000,
                30000,
                1,
                10,
                1,
                21
        );
        when(teamRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));
        when(teamRequiredRoleRepository.findByTeamIdIn(List.of(first.getId(), second.getId())))
                .thenReturn(List.of(
                        requiredRole(first, PlayerRole.AWPER),
                        requiredRole(second, PlayerRole.RIFLER)
                ));

        Page<TeamResponse> response = teamService.listingTeams(filter, pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).requiredRoles()).containsExactly(PlayerRole.AWPER);
        assertThat(response.getContent().get(1).requiredRoles()).containsExactly(PlayerRole.RIFLER);
    }

    @Test
    void listingTeamsDoesNotLookupRolesWhenPageIsEmpty() {
        PageRequest pageable = PageRequest.of(0, 10);
        TeamFilter filter = new TeamFilter(null, null, null, null, null, null, null, null, null, null, null);
        when(teamRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<TeamResponse> response = teamService.listingTeams(filter, pageable);

        assertThat(response).isEmpty();
        verify(teamRequiredRoleRepository, never()).findByTeamIdIn(anyList());
    }

    @Test
    void listingTeamsRejectsInactiveStatusFilter() {
        TeamFilter filter = new TeamFilter(null, null, TeamStatus.INACTIVE, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> teamService.listingTeams(filter, PageRequest.of(0, 10)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Filtro de status inválido.");

        verify(teamRepository, never()).findAll(any(Specification.class), any(PageRequest.class));
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
                .hasMessage("Usuário não ativo.");

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
                .hasMessage("Time inativo.");

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
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não é o dono do time.");

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
        UpdateTeamRequest request = new UpdateTeamRequest(null, null, null, null, null, null, null, null, null, null, null, TeamStatus.INACTIVE);

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
        UpdateTeamRequest request = new UpdateTeamRequest(null, null, null, null, null, 20000, 19000, null, null, null, null, null);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("minPremierRating must be less than or equal to maxPremierRating");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void editTeamRejectsBlankNameDescriptionAndRegion() {
        assertEditValidationThrows(
                new UpdateTeamRequest(" ", null, null, null, null, null, null, null, null, null, null, null),
                "O nome do time não pode ser vazio."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", " ", null, null, null, null, null, null, null, null, null, null),
                "A descrição do time não pode ser vazio."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, " ", null, null, null, null, null, null, null),
                "A região do time não pode ser vazio."
        );
    }

    @Test
    void editTeamRejectsUnchangedFields() {
        assertEditValidationThrows(
                new UpdateTeamRequest("Kyofuse Academy", null, null, null, null, null, null, null, null, null, null, null),
                "O nome do time não foi alterado."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Development team", null, null, null, null, null, null, null, null, null, null),
                "A descrição do time não foi alterada."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "BR", null, null, null, null, null, null, null),
                "A região do time não foi alterada."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, null, null, null, null, TeamStatus.ACTIVE),
                "O status do time não foi alterado."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, null, null, 1, null, null),
                "O minGcRank do time não foi alterado."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, null, null, null, 21, null),
                "O maxGcRank do time não foi alterado."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, 1, null, null, null, null),
                "O minFaceitLevel do time não foi alterado."
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, null, 10, null, null, null),
                "O maxFaceitLevel do time não foi alterado."
        );
    }

    @Test
    void editTeamRejectsInvalidFaceitAndGcRanges() {
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, 9, 4, null, null, null),
                "minFaceitLevel must be less than or equal to maxFaceitLevel"
        );
        assertEditValidationThrows(
                new UpdateTeamRequest("Updated", "Updated description", null, null, "NA", null, null, null, null, 15, 3, null),
                "minGcRank must be less than or equal to maxGcRank"
        );
    }

    @Test
    void editTeamAcceptsOpenEndedFaceitAndGcRanges() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = activeTeam(teamId, owner);
        UpdateTeamRequest request = new UpdateTeamRequest("Updated Academy", "Updated description", null, null, "NA", null, null, 2, null, 2, null, null);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.editTeam(userId, teamId, request);

        assertThat(response.minFaceitLevel()).isEqualTo(2);
        assertThat(response.maxFaceitLevel()).isNull();
        assertThat(response.minGcRank()).isEqualTo(2);
        assertThat(response.maxGcRank()).isNull();
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
                .hasMessage("Usuário não ativo.");

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
                .hasMessage("Time inativo.");

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
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não é o dono do time.");

        verify(teamRepository, never()).save(any());
    }

    @Test
    void manageRequiredRolesReplacesRolesAndUpdatesTeamTimestamp() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = activeTeam(teamId, owner);
        List<TeamRequiredRole> currentRoles = new ArrayList<>(List.of(requiredRole(team, PlayerRole.AWPER)));
        UpdateTeamRequiredRolesRequest request = new UpdateTeamRequiredRolesRequest(List.of(PlayerRole.RIFLER, PlayerRole.SUPPORT));

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(currentRoles);
        when(teamRequiredRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse response = teamService.manageRequiredRoles(userId, teamId, request);

        verify(teamRequiredRoleRepository).deleteAll(currentRoles);
        verify(teamRequiredRoleRepository).flush();
        assertThat(response.requiredRoles()).containsExactly(PlayerRole.RIFLER, PlayerRole.SUPPORT);
        assertThat(team.getUpdatedAt()).isNotNull();
    }

    @Test
    void manageRequiredRolesRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(UserStatus.INACTIVE)
                .build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> teamService.manageRequiredRoles(
                userId,
                UUID.randomUUID(),
                new UpdateTeamRequiredRolesRequest(List.of(PlayerRole.AWPER))
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não ativo.");

        verify(teamRepository, never()).findById(any());
    }

    @Test
    void manageRequiredRolesRejectsMissingTeamInactiveTeamAndNonOwner() {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User user = activeUser(userId, "user");
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.manageRequiredRoles(
                userId,
                teamId,
                new UpdateTeamRequiredRolesRequest(List.of(PlayerRole.AWPER))
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Time não encontrado: " + teamId);

        Team inactiveTeam = activeTeam(teamId, user);
        inactiveTeam.setStatus(TeamStatus.INACTIVE);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(inactiveTeam));

        assertThatThrownBy(() -> teamService.manageRequiredRoles(
                userId,
                teamId,
                new UpdateTeamRequiredRolesRequest(List.of(PlayerRole.AWPER))
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Time inativo.");

        User owner = activeUser(UUID.randomUUID(), "owner");
        Team otherOwnerTeam = activeTeam(teamId, owner);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(otherOwnerTeam));

        assertThatThrownBy(() -> teamService.manageRequiredRoles(
                userId,
                teamId,
                new UpdateTeamRequiredRolesRequest(List.of(PlayerRole.AWPER))
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não é o dono do time.");
    }

    @Test
    void manageRequiredRolesRejectsInvalidRoleLists() {
        assertManageRequiredRolesValidationThrows(null, "requiredRoles não pode ser nulo.");
        ArrayList<PlayerRole> rolesWithNull = new ArrayList<>();
        rolesWithNull.add(PlayerRole.AWPER);
        rolesWithNull.add(null);
        assertManageRequiredRolesValidationThrows(rolesWithNull, "requiredRoles não pode conter valores nulos.");
        assertManageRequiredRolesValidationThrows(
                List.of(PlayerRole.AWPER, PlayerRole.AWPER),
                "requiredRoles não pode conter roles duplicadas."
        );
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<TeamRequiredRole>> rolesCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    private void assertEditValidationThrows(UpdateTeamRequest request, String message) {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = activeTeam(teamId, owner);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamFinder.findTeamById(teamId)).thenReturn(team);
        when(teamRequiredRoleRepository.findByTeamId(teamId)).thenReturn(List.of());

        assertThatThrownBy(() -> teamService.editTeam(userId, teamId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(message);
    }

    private void assertManageRequiredRolesValidationThrows(List<PlayerRole> roles, String message) {
        UUID userId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = activeTeam(teamId, owner);
        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamService.manageRequiredRoles(
                userId,
                teamId,
                new UpdateTeamRequiredRolesRequest(roles)
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(message);

        verify(teamRequiredRoleRepository, never()).deleteAll(anyList());
    }

    private TeamRequiredRole requiredRole(Team team, PlayerRole role) {
        return TeamRequiredRole.builder()
                .team(team)
                .roleName(role)
                .build();
    }

    private User activeUser(UUID userId, String username) {
        return User.builder()
                .id(userId)
                .username(username)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private TeamRequest validRequest(List<PlayerRole> requiredRoles) {
        return new TeamRequest(
                "Kyofuse Academy",
                null,
                null,
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
        return new UpdateTeamRequest("Updated Academy", "Updated description", null, null, "NA", 12000, 25000, 4, 9, 5, 18, TeamStatus.CLOSED);
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
