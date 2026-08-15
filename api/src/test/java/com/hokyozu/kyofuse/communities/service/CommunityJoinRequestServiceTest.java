package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.response.CommunityJoinRequestResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityJoinRequest;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityJoinRequestRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityJoinRequestServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private CommunityMemberService communityMemberService;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private CommunityJoinRequestRepository communityJoinRequestRepository;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private CommunityJoinRequestService communityJoinRequestService;

    @Test
    void requestToJoinCommunityCreatesPendingRequestForPrivateCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "requester");
        Community community = community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());
        when(communityJoinRequestRepository.existsByCommunityIdAndRequesterId(communityId, userId)).thenReturn(false);
        when(communityJoinRequestRepository.save(org.mockito.ArgumentMatchers.any(CommunityJoinRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityJoinRequestResponse response = communityJoinRequestService.requestToJoinCommunity(userId, communityId);

        ArgumentCaptor<CommunityJoinRequest> captor = ArgumentCaptor.forClass(CommunityJoinRequest.class);
        verify(communityJoinRequestRepository).save(captor.capture());
        verify(blockValidator).validate(user, community.getOwner());
        assertThat(captor.getValue().getRequester()).isSameAs(user);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.communityId()).isEqualTo(communityId);
    }

    @Test
    void requestToJoinCommunityRejectsArchivedCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "requester"));
        when(communityRepository.findById(communityId))
                .thenReturn(Optional.of(community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ARCHIVED)));

        assertThatThrownBy(() -> communityJoinRequestService.requestToJoinCommunity(userId, communityId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot request to join an archived community");
    }

    @Test
    void requestToJoinCommunityRejectsPublicCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "requester"));
        when(communityRepository.findById(communityId))
                .thenReturn(Optional.of(community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE)));

        assertThatThrownBy(() -> communityJoinRequestService.requestToJoinCommunity(userId, communityId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("This community is public, join it directly instead of requesting");
    }

    @Test
    void requestToJoinCommunityRejectsWhenAlreadyActiveMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "requester");
        Community community = community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityMember membership = member(community, user, CommunityMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> communityJoinRequestService.requestToJoinCommunity(userId, communityId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is already a member of the community");
    }

    @Test
    void requestToJoinCommunityRejectsBannedUser() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "requester");
        Community community = community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityMember membership = member(community, user, CommunityMemberStatus.BANNED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> communityJoinRequestService.requestToJoinCommunity(userId, communityId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is banned from this community");
    }

    @Test
    void requestToJoinCommunityAllowsReapplyingAfterLeaving() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "requester");
        Community community = community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityMember membership = member(community, user, CommunityMemberStatus.LEFT);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.of(membership));
        when(communityJoinRequestRepository.existsByCommunityIdAndRequesterId(communityId, userId)).thenReturn(false);
        when(communityJoinRequestRepository.save(org.mockito.ArgumentMatchers.any(CommunityJoinRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommunityJoinRequestResponse response = communityJoinRequestService.requestToJoinCommunity(userId, communityId);

        assertThat(response.userId()).isEqualTo(userId);
        verify(communityJoinRequestRepository).save(org.mockito.ArgumentMatchers.any(CommunityJoinRequest.class));
    }

    @Test
    void requestToJoinCommunityRejectsDuplicatePendingRequest() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "requester");
        Community community = community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());
        when(communityJoinRequestRepository.existsByCommunityIdAndRequesterId(communityId, userId)).thenReturn(true);

        assertThatThrownBy(() -> communityJoinRequestService.requestToJoinCommunity(userId, communityId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("A join request for this community is already pending");

        verify(communityJoinRequestRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveJoinRequestAddsMemberAndDeletesRequestForStaff() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        User staff = activeUser(userId, "moderator");
        User requester = activeUser(UUID.randomUUID(), "requester");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityJoinRequest joinRequest = CommunityJoinRequest.builder()
                .id(requestId)
                .community(community)
                .requester(requester)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(staff);
        when(communityJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
        when(communityMemberService.isOwnerOrStaff(staff, community)).thenReturn(true);

        communityJoinRequestService.approveJoinRequest(userId, requestId);

        verify(blockValidator).validate(staff, requester);
        verify(communityMemberService).addMember(requester, community);
        verify(communityJoinRequestRepository).delete(joinRequest);
    }

    @Test
    void approveJoinRequestRejectsNonStaffRequester() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        User requester = activeUser(UUID.randomUUID(), "requester");
        User caller = activeUser(userId, "regular-member");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityJoinRequest joinRequest = CommunityJoinRequest.builder()
                .id(requestId)
                .community(community)
                .requester(requester)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(caller);
        when(communityJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
        when(communityMemberService.isOwnerOrStaff(caller, community)).thenReturn(false);

        assertThatThrownBy(() -> communityJoinRequestService.approveJoinRequest(userId, requestId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the community owner, an admin or a moderator can approve join requests");

        verify(communityMemberService, never()).addMember(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(communityJoinRequestRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void approveJoinRequestRejectsMissingRequest() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "staff"));
        when(communityJoinRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityJoinRequestService.approveJoinRequest(userId, requestId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Join request not found");
    }

    @Test
    void rejectJoinRequestDeletesRequestForStaff() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        User staff = activeUser(userId, "moderator");
        User requester = activeUser(UUID.randomUUID(), "requester");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityJoinRequest joinRequest = CommunityJoinRequest.builder()
                .id(requestId)
                .community(community)
                .requester(requester)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(staff);
        when(communityJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
        when(communityMemberService.isOwnerOrStaff(staff, community)).thenReturn(true);

        communityJoinRequestService.rejectJoinRequest(userId, requestId);

        verify(communityJoinRequestRepository).delete(joinRequest);
        verify(communityMemberService, never()).addMember(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectJoinRequestRejectsNonStaffRequester() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        User caller = activeUser(userId, "regular-member");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);
        CommunityJoinRequest joinRequest = CommunityJoinRequest.builder()
                .id(requestId)
                .community(community)
                .requester(activeUser(UUID.randomUUID(), "requester"))
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(caller);
        when(communityJoinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
        when(communityMemberService.isOwnerOrStaff(caller, community)).thenReturn(false);

        assertThatThrownBy(() -> communityJoinRequestService.rejectJoinRequest(userId, requestId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the community owner, an admin or a moderator can reject join requests");

        verify(communityJoinRequestRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectJoinRequestRejectsMissingRequest() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "staff"));
        when(communityJoinRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityJoinRequestService.rejectJoinRequest(userId, requestId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Join request not found");
    }

    private User activeUser(UUID userId, String username) {
        return User.builder()
                .id(userId)
                .username(username)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private Community community(UUID communityId, CommunityVisibility visibility, CommunityStatus status) {
        Instant now = Instant.now();
        return Community.builder()
                .id(communityId)
                .owner(activeUser(UUID.randomUUID(), "owner"))
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .visibility(visibility)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private CommunityMember member(Community community, User user, CommunityMemberStatus status) {
        Instant now = Instant.now();
        return CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .status(status)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
