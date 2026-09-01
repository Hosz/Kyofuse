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
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.validator.CommunityCreationValidator;
import com.hokyozu.kyofuse.communities.validator.CommunityEditValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
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
    private ConversationService conversationService;

    @Mock
    private com.hokyozu.kyofuse.storage.service.ImageProcessingService imageProcessingService;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private CommunityService communityService;

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
