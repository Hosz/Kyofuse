package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.chat.service.ConversationService;
import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityJoinRequestRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.repository.UserPinnedCommunityRepository;
import com.hokyozu.kyofuse.communities.validator.CommunityCreationValidator;
import com.hokyozu.kyofuse.communities.validator.CommunityEditValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import com.hokyozu.kyofuse.teams.service.TeamChecker;
import com.hokyozu.kyofuse.teams.service.TeamService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private CommunityCreationValidator communityCreationValidator;

    @Mock
    private CommunityEditValidator communityEditValidator;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private CommunityJoinRequestRepository communityJoinRequestRepository;

    @Mock
    private UserPinnedCommunityRepository userPinnedCommunityRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private com.hokyozu.kyofuse.storage.service.ImageProcessingService imageProcessingService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamChecker teamChecker;

    @Mock
    private TeamFinder teamFinder;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private TeamRequiredRoleRepository teamRequiredRoleRepository;

    @Mock
    private TeamService teamService;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private CommunityService communityService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(communityService, "teamService", teamService);
    }

    @Test
    void createCommunitySavesActiveCommunityForActiveUser() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "owner");
        CommunityRequest request = validRequest();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
            Community community = invocation.getArgument(0);
            community.setId(communityId);
            return community;
        });

        CommunityResponse response = communityService.createCommunity(request, userId);

        ArgumentCaptor<Community> captor = ArgumentCaptor.forClass(Community.class);
        verify(communityCreationValidator).validate(request);
        verify(communityRepository).save(captor.capture());

        Community saved = captor.getValue();
        verify(conversationService).createCommunityConversation(saved, user);
        assertThat(saved.getOwner()).isSameAs(user);
        assertThat(saved.getName()).isEqualTo("Kyofuse CS2");
        assertThat(saved.getSlug()).isEqualTo("kyofuse-cs2");
        assertThat(saved.getStatus()).isEqualTo(CommunityStatus.ACTIVE);
        assertThat(response.id()).isEqualTo(communityId);
        assertThat(response.ownerUsername()).isEqualTo("owner");
    }

    @Test
    void createCommunityRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).status(UserStatus.INACTIVE).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> communityService.createCommunity(validRequest(), userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não ativo.");

        verify(communityCreationValidator, never()).validate(any());
        verify(communityRepository, never()).save(any());
    }

    @Test
    void autoCreateTeamCommunityUsesTeamSlugWhenAvailable() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Team team = team(owner, "Kyofuse Academy", "kyofuse-academy");

        when(communityRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Community community = communityService.autoCreateTeamCommunity(owner, team);

        assertThat(community.getSlug()).isEqualTo("kyofuse-academy");
        assertThat(community.getTeam()).isEqualTo(team);
        assertThat(community.getOwner()).isEqualTo(owner);
    }

    @Test
    void autoCreateTeamCommunityAppendsSuffixWhenSlugAlreadyTakenByAStandaloneCommunity() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Team team = team(owner, "Kyofuse Academy", "kyofuse-academy");

        when(communityRepository.existsBySlug("kyofuse-academy")).thenReturn(true);
        when(communityRepository.existsBySlug("kyofuse-academy-2")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Community community = communityService.autoCreateTeamCommunity(owner, team);

        assertThat(community.getSlug()).isEqualTo("kyofuse-academy-2");
    }

    @Test
    void autoCreateTeamCommunityTriesNextSuffixWhenFirstFallbackAlsoCollides() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Team team = team(owner, "Kyofuse Academy", "kyofuse-academy");

        when(communityRepository.existsBySlug("kyofuse-academy")).thenReturn(true);
        when(communityRepository.existsBySlug("kyofuse-academy-2")).thenReturn(true);
        when(communityRepository.existsBySlug("kyofuse-academy-3")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Community community = communityService.autoCreateTeamCommunity(owner, team);

        assertThat(community.getSlug()).isEqualTo("kyofuse-academy-3");
    }

    @Test
    void autoCreateTeamCommunityTruncatesBaseSlugWhenSuffixWouldExceedColumnLength() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        String hundredCharSlug = "a".repeat(100);
        Team team = team(owner, "Kyofuse Academy", hundredCharSlug);

        when(communityRepository.existsBySlug(hundredCharSlug)).thenReturn(true);
        when(communityRepository.existsBySlug("a".repeat(98) + "-2")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Community community = communityService.autoCreateTeamCommunity(owner, team);

        assertThat(community.getSlug()).hasSize(100);
        assertThat(community.getSlug()).isEqualTo("a".repeat(98) + "-2");
    }

    @Test
    void editCommunityUpdatesFieldsForOwner() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Community community = activeCommunity(communityId, owner);
        UpdateCommunityRequest request = new UpdateCommunityRequest("Updated Name", null, null, null, null, null);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityResponse response = communityService.editCommunity(userId, communityId, request);

        verify(communityEditValidator).validate(request, community);
        verify(communityRepository).save(community);
        assertThat(community.getName()).isEqualTo("Updated Name");
        assertThat(response.communityName()).isEqualTo("Updated Name");
    }

    @Test
    void editCommunityRejectsWhenCommunityNotFound() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.editCommunity(userId, communityId, new UpdateCommunityRequest(null, null, null, null, null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");

        verify(communityRepository, never()).save(any());
    }

    @Test
    void editCommunityRejectsNonOwnerAsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User requester = activeUser(userId, "requester");
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = activeCommunity(communityId, owner);

        when(userFinder.findProfileByUserId(userId)).thenReturn(requester);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.editCommunity(userId, communityId, new UpdateCommunityRequest(null, null, null, null, null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");

        verify(communityRepository, never()).save(any());
    }

    @Test
    void detailCommunityReturnsResponseForActiveCommunity() {
        UUID communityId = UUID.randomUUID();
        Community community = activeCommunity(communityId, activeUser(UUID.randomUUID(), "owner"));
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        CommunityResponse response = communityService.detailCommunity(communityId.toString());

        assertThat(response.id()).isEqualTo(communityId);
    }

    @Test
    void detailCommunityThrowsNotFoundWhenMissing() {
        UUID communityId = UUID.randomUUID();
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.detailCommunity(communityId.toString()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");
    }

    @Test
    void detailCommunityThrowsNotFoundWhenArchived() {
        UUID communityId = UUID.randomUUID();
        Community community = activeCommunity(communityId, activeUser(UUID.randomUUID(), "owner"));
        community.setStatus(CommunityStatus.ARCHIVED);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.detailCommunity(communityId.toString()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");
    }

    @Test
    void deleteCommunityRemovesCommunityForOwner() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Community community = activeCommunity(communityId, owner);
        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.deleteCommunity(userId, communityId);

        verify(communityRepository).delete(community);
    }

    @Test
    void deleteCommunityRejectsNonOwnerAsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User requester = User.builder().id(userId).username("requester").build();
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = activeCommunity(communityId, owner);
        when(userFinder.findProfileByUserId(userId)).thenReturn(requester);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.deleteCommunity(userId, communityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");

        verify(communityRepository, never()).delete(any());
    }

    @Test
    void deleteCommunityWithTeamDetachesWhenDeleteTeamIsFalse() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = Team.builder().id(UUID.randomUUID()).owner(owner).build();
        Community community = activeCommunity(communityId, owner);
        community.setTeam(team);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.deleteCommunity(userId, communityId, false);

        assertThat(community.getTeam()).isNull();
        verify(communityRepository).delete(community);
        verify(teamService, never()).deleteTeam(any(), any(UUID.class), any(boolean.class));
    }

    @Test
    void deleteCommunityWithTeamDeletesBothWhenUserIsOwnerOfBoth() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = Team.builder().id(UUID.randomUUID()).owner(owner).build();
        Community community = activeCommunity(communityId, owner);
        community.setTeam(team);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.deleteCommunity(userId, communityId, true);

        verify(teamService).deleteTeam(userId, team.getId(), false);
        verify(communityRepository).delete(community);
    }

    @Test
    void deleteCommunityWithTeamRejectsDeletingTeamWhenNotTeamOwner() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        User teamOwner = activeUser(UUID.randomUUID(), "teamOwner");
        Team team = Team.builder().id(UUID.randomUUID()).owner(teamOwner).build();
        Community community = activeCommunity(communityId, owner);
        community.setTeam(team);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.deleteCommunity(userId, communityId, true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Apenas o dono do time pode solicitar a exclusão do mesmo.");

        verify(communityRepository, never()).delete(any());
        verify(teamService, never()).deleteTeam(any(), any(UUID.class), any(boolean.class));
    }

    @Test
    void deleteCommunityByIdentifierResolvesAndDeletes() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Community community = activeCommunity(communityId, owner);

        when(communityRepository.findBySlug("comm-slug")).thenReturn(Optional.of(community));
        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.deleteCommunity(userId, "comm-slug", false);

        verify(communityRepository).delete(community);
    }

    @Test
    void archiveCommunityByIdentifierResolvesAndArchives() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Community community = activeCommunity(communityId, owner);

        when(communityRepository.findBySlug("comm-slug")).thenReturn(Optional.of(community));
        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.archiveCommunity(userId, "comm-slug");

        assertThat(community.getStatus()).isEqualTo(CommunityStatus.ARCHIVED);
        verify(communityRepository).save(community);
    }

    @Test
    void detachTeamCommunityByCommunityDetachesSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Team team = Team.builder().id(UUID.randomUUID()).owner(owner).build();
        Community community = activeCommunity(communityId, owner);
        community.setTeam(team);

        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.detachTeamCommunityByCommunity(userId, communityId.toString());

        assertThat(community.getTeam()).isNull();
        verify(communityRepository).save(community);
    }

    @Test
    void archiveCommunitySetsArchivedStatusForOwner() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(userId, "owner");
        Community community = activeCommunity(communityId, owner);
        when(userFinder.findProfileByUserId(userId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        communityService.archiveCommunity(userId, communityId);

        assertThat(community.getStatus()).isEqualTo(CommunityStatus.ARCHIVED);
        verify(communityRepository).save(community);
    }

    @Test
    void archiveCommunityRejectsNonOwnerAsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User requester = User.builder().id(userId).username("requester").build();
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = activeCommunity(communityId, owner);
        when(userFinder.findProfileByUserId(userId)).thenReturn(requester);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.archiveCommunity(userId, communityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");

        verify(communityRepository, never()).save(any());
    }

    @Test
    void listCommunitiesReturnsActiveCommunitiesForActiveUser() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "viewer");
        Community community = activeCommunity(UUID.randomUUID(), activeUser(UUID.randomUUID(), "owner"));
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findAllByStatus(CommunityStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(community), pageable, 1));

        var response = communityService.listCommunities(userId, null, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().id()).isEqualTo(community.getId());
    }

    @Test
    void listCommunitiesRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).status(UserStatus.INACTIVE).build();
        Pageable pageable = PageRequest.of(0, 20);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> communityService.listCommunities(userId, null, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não ativo.");

        verify(communityRepository, never()).findAllByStatus(any(), any());
    }

    @Test
    void listMyCommunitiesReturnsCommunitiesOfActiveMemberships() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "viewer");
        Community community = activeCommunity(UUID.randomUUID(), activeUser(UUID.randomUUID(), "owner"));
        CommunityMember membership = CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityMemberRepository.findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(membership), pageable, 1));

        var response = communityService.listMyCommunities(userId, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().id()).isEqualTo(community.getId());
    }

    @Test
    void listMyCommunitiesRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).status(UserStatus.INACTIVE).build();
        Pageable pageable = PageRequest.of(0, 20);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> communityService.listMyCommunities(userId, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não ativo.");

        verify(communityMemberRepository, never()).findByUserAndStatus(any(), any(), any());
    }

    @Test
    void createCommunityAlsoRegistersOwnerAsAdminMember() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "owner");

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        communityService.createCommunity(validRequest(), userId);

        ArgumentCaptor<CommunityMember> captor = ArgumentCaptor.forClass(CommunityMember.class);
        verify(communityMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getRole()).isEqualTo(CommunityMemberRole.ADMIN);
        assertThat(captor.getValue().getStatus()).isEqualTo(CommunityMemberStatus.ACTIVE);
    }

    @Test
    void autoCreateTeamCommunityAlsoRegistersOwnerAsAdminMember() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Team team = team(owner, "Kyofuse Academy", "kyofuse-academy");

        when(communityRepository.existsBySlug("kyofuse-academy")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        communityService.autoCreateTeamCommunity(owner, team);

        ArgumentCaptor<CommunityMember> captor = ArgumentCaptor.forClass(CommunityMember.class);
        verify(communityMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(owner);
        assertThat(captor.getValue().getRole()).isEqualTo(CommunityMemberRole.ADMIN);
    }

    @Test
    void listCommunitiesFiltersByNameWhenProvided() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "viewer");
        Community community = activeCommunity(UUID.randomUUID(), activeUser(UUID.randomUUID(), "owner"));
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findAllByStatusAndNameContainingIgnoreCase(CommunityStatus.ACTIVE, "kyo", pageable))
                .thenReturn(new PageImpl<>(List.of(community), pageable, 1));

        var response = communityService.listCommunities(userId, "  kyo  ", pageable);

        assertThat(response.getContent()).hasSize(1);
        verify(communityRepository, never()).findAllByStatus(any(), any());
    }

    @Test
    void listCommunitiesIgnoresBlankNameFilter() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "viewer");
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findAllByStatus(CommunityStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        communityService.listCommunities(userId, "   ", pageable);

        verify(communityRepository, never()).findAllByStatusAndNameContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void detailCommunityByTeamReturnsLinkedCommunity() {
        UUID teamId = UUID.randomUUID();
        Community community = activeCommunity(UUID.randomUUID(), activeUser(UUID.randomUUID(), "owner"));
        when(communityRepository.findByTeamId(teamId)).thenReturn(Optional.of(community));

        var response = communityService.detailCommunityByTeam(teamId);

        assertThat(response.id()).isEqualTo(community.getId());
    }

    @Test
    void detailCommunityByTeamThrowsNotFoundWhenTeamHasNoCommunity() {
        UUID teamId = UUID.randomUUID();
        when(communityRepository.findByTeamId(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityService.detailCommunityByTeam(teamId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");
    }

    @Test
    void detailCommunityByTeamThrowsNotFoundWhenArchived() {
        UUID teamId = UUID.randomUUID();
        Community community = activeCommunity(UUID.randomUUID(), activeUser(UUID.randomUUID(), "owner"));
        community.setStatus(CommunityStatus.ARCHIVED);
        when(communityRepository.findByTeamId(teamId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.detailCommunityByTeam(teamId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listUserCommunitiesReturnsCommunitiesOfTargetUser() {
        UUID viewerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        User viewer = activeUser(viewerId, "viewer");
        Community community = activeCommunity(UUID.randomUUID(), activeUser(UUID.randomUUID(), "owner"));
        CommunityMember membership = CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(activeUser(targetId, "target"))
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(viewerId)).thenReturn(viewer);
        when(communityMemberRepository.findByUserIdAndStatus(targetId, CommunityMemberStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(membership), pageable, 1));

        var response = communityService.listUserCommunities(viewerId, targetId, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().id()).isEqualTo(community.getId());
    }

    @Test
    void createCommunityFromTeam_createsCommunityAndSyncsMembers() {
        UUID ownerId = UUID.randomUUID();
        User owner = activeUser(ownerId, "teamowner");
        Team team = team(owner, "Furia Academy", "furia-academy");

        UUID managerId = UUID.randomUUID();
        User manager = activeUser(managerId, "teammanager");
        TeamMember tmManager = TeamMember.builder()
                .team(team)
                .user(manager)
                .memberType(TeamMemberType.MANAGER)
                .status(TeamMemberStatus.ACTIVE)
                .build();

        UUID playerId = UUID.randomUUID();
        User player = activeUser(playerId, "teamplayer");
        TeamMember tmPlayer = TeamMember.builder()
                .team(team)
                .user(player)
                .memberType(TeamMemberType.PLAYER)
                .status(TeamMemberStatus.ACTIVE)
                .build();

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(teamFinder.findTeamByIdentifier("furia-academy")).thenReturn(team);
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.empty());
        when(communityRepository.existsBySlug("furia-academy")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teamMemberRepository.findByTeamAndStatus(team, TeamMemberStatus.ACTIVE))
                .thenReturn(List.of(tmManager, tmPlayer));

        CommunityResponse response = communityService.createCommunityFromTeam(ownerId, "furia-academy");

        assertThat(response.communityName()).isEqualTo("Furia Academy");
        assertThat(response.communitySlug()).isEqualTo("furia-academy");
        assertThat(response.teamId()).isEqualTo(team.getId());

        verify(conversationService).createCommunityConversation(any(Community.class), any(User.class));

        ArgumentCaptor<CommunityMember> memberCaptor = ArgumentCaptor.forClass(CommunityMember.class);
        verify(communityMemberRepository, org.mockito.Mockito.atLeast(3)).save(memberCaptor.capture());

        List<CommunityMember> savedMembers = memberCaptor.getAllValues();
        assertThat(savedMembers).anyMatch(m -> m.getUser().getId().equals(ownerId) && m.getRole() == CommunityMemberRole.ADMIN);
        assertThat(savedMembers).anyMatch(m -> m.getUser().getId().equals(managerId) && m.getRole() == CommunityMemberRole.ADMIN);
        assertThat(savedMembers).anyMatch(m -> m.getUser().getId().equals(playerId) && m.getRole() == CommunityMemberRole.MEMBER);
    }

    @Test
    void createCommunityFromTeam_failsWhenTeamAlreadyHasCommunity() {
        UUID ownerId = UUID.randomUUID();
        User owner = activeUser(ownerId, "teamowner");
        Team team = team(owner, "Furia Academy", "furia-academy");

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(teamFinder.findTeamByIdentifier("furia-academy")).thenReturn(team);
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.of(activeCommunity(UUID.randomUUID(), owner)));

        assertThatThrownBy(() -> communityService.createCommunityFromTeam(ownerId, "furia-academy"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createCommunityFromTeam_failsWhenUserNotTeamHead() {
        UUID callerId = UUID.randomUUID();
        User caller = activeUser(callerId, "regularuser");
        User owner = activeUser(UUID.randomUUID(), "teamowner");
        Team team = team(owner, "Furia Academy", "furia-academy");

        when(userFinder.findProfileByUserId(callerId)).thenReturn(caller);
        when(teamFinder.findTeamByIdentifier("furia-academy")).thenReturn(team);
        when(teamMemberRepository.findByTeamAndUser(team, caller)).thenReturn(null);

        assertThatThrownBy(() -> communityService.createCommunityFromTeam(callerId, "furia-academy"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void attachTeamAndCommunity_attachesAndIntegratesMutualHeads() {
        UUID callerId = UUID.randomUUID();
        User caller = activeUser(callerId, "mutualhead");

        UUID teamOwnerId = callerId;
        User teamOwner = caller;
        Team team = team(teamOwner, "Team Alpha", "team-alpha");

        UUID commOwnerId = UUID.randomUUID();
        User commOwner = activeUser(commOwnerId, "commowner");
        Community community = activeCommunity(UUID.randomUUID(), commOwner);

        // Caller is team owner and community admin
        CommunityMember callerCommMember = CommunityMember.builder()
                .community(community)
                .user(caller)
                .role(CommunityMemberRole.ADMIN)
                .status(CommunityMemberStatus.ACTIVE)
                .build();

        UUID teamManagerId = UUID.randomUUID();
        User teamManager = activeUser(teamManagerId, "manager1");
        TeamMember tmManager = TeamMember.builder()
                .team(team)
                .user(teamManager)
                .memberType(TeamMemberType.MANAGER)
                .status(TeamMemberStatus.ACTIVE)
                .build();

        when(userFinder.findProfileByUserId(callerId)).thenReturn(caller);
        when(teamFinder.findTeamByIdentifier("team-alpha")).thenReturn(team);
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.empty());
        when(communityMemberRepository.findByCommunityAndUser(community, caller)).thenReturn(Optional.of(callerCommMember));
        when(communityMemberRepository.findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE))
                .thenReturn(List.of(callerCommMember));
        when(teamMemberRepository.findByTeamAndStatus(team, TeamMemberStatus.ACTIVE))
                .thenReturn(List.of(tmManager));
        when(communityRepository.save(any(Community.class))).thenAnswer(inv -> inv.getArgument(0));

        CommunityResponse response = communityService.attachTeamAndCommunity(callerId, "team-alpha", community.getId().toString());

        assertThat(response.id()).isEqualTo(community.getId());
        assertThat(community.getTeam()).isSameAs(team);

        // Heads of team viram ADMIN na comunidade
        verify(communityMemberRepository, org.mockito.Mockito.atLeastOnce()).save(any(CommunityMember.class));
        // Heads da comunidade viram MANAGER no time
        verify(teamMemberRepository, org.mockito.Mockito.atLeastOnce()).save(any(TeamMember.class));
    }

    @Test
    void attachTeamAndCommunity_failsWhenAlreadyConnected() {
        UUID callerId = UUID.randomUUID();
        User caller = activeUser(callerId, "user");
        Team team = team(caller, "Team Alpha", "team-alpha");
        Community community = activeCommunity(UUID.randomUUID(), caller);

        when(userFinder.findProfileByUserId(callerId)).thenReturn(caller);
        when(teamFinder.findTeamByIdentifier("team-alpha")).thenReturn(team);
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityService.attachTeamAndCommunity(callerId, "team-alpha", community.getId().toString()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void listAvailableCommunitiesForTeam_returnsAvailable() {
        UUID callerId = UUID.randomUUID();
        User caller = activeUser(callerId, "teamowner");
        Team team = team(caller, "Team Alpha", "team-alpha");
        Community community = activeCommunity(UUID.randomUUID(), caller);

        when(userFinder.findProfileByUserId(callerId)).thenReturn(caller);
        when(teamFinder.findTeamByIdentifier("team-alpha")).thenReturn(team);
        when(communityRepository.findByTeamId(team.getId())).thenReturn(Optional.empty());
        when(communityRepository.findAvailableForTeam(callerId)).thenReturn(List.of(community));

        List<CommunityResponse> result = communityService.listAvailableCommunitiesForTeam(callerId, "team-alpha");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(community.getId());
    }

    @Test
    void listAvailableTeamsForCommunity_returnsAvailable() {
        UUID callerId = UUID.randomUUID();
        User caller = activeUser(callerId, "commowner");
        Community community = activeCommunity(UUID.randomUUID(), caller);
        Team team = team(caller, "Team Alpha", "team-alpha");

        when(userFinder.findProfileByUserId(callerId)).thenReturn(caller);
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(teamRepository.findAvailableForCommunity(callerId)).thenReturn(List.of(team));
        when(teamRequiredRoleRepository.findByTeamId(team.getId())).thenReturn(List.of());

        List<com.hokyozu.kyofuse.teams.dto.response.TeamResponse> result = communityService.listAvailableTeamsForCommunity(callerId, community.getId().toString());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(team.getId());
    }

    private CommunityRequest validRequest() {
        return new CommunityRequest(
                "Kyofuse CS2",
                "kyofuse-cs2",
                "Community description",
                null,
                null,
                CommunityVisibility.PUBLIC
        );
    }

    private User activeUser(UUID userId, String username) {
        return User.builder()
                .id(userId)
                .username(username)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private Team team(User owner, String name, String slug) {
        return Team.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .name(name)
                .slug(slug)
                .status(TeamStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Community activeCommunity(UUID communityId, User owner) {
        Instant now = Instant.now();
        return Community.builder()
                .id(communityId)
                .owner(owner)
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
