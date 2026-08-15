package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
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
import org.springframework.data.domain.Page;
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
class CommunityMemberServiceTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private UserFinder userFinder;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private CommunityMemberService communityMemberService;

    @Test
    void joinCommunityCreatesActiveMemberForPublicCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "player");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());
        when(communityMemberRepository.save(any(CommunityMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityMemberResponse response = communityMemberService.joinCommunity(userId, communityId);

        ArgumentCaptor<CommunityMember> captor = ArgumentCaptor.forClass(CommunityMember.class);
        verify(communityMemberRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CommunityMemberStatus.ACTIVE);
        assertThat(captor.getValue().getRole()).isEqualTo(CommunityMemberRole.MEMBER);
        assertThat(response.memberId()).isEqualTo(userId);
    }

    @Test
    void joinCommunityRejectsArchivedCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "player");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ARCHIVED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityMemberService.joinCommunity(userId, communityId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot join an archived community");

        verify(communityMemberRepository, never()).save(any());
    }

    @Test
    void joinCommunityRejectsPrivateCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "player");
        Community community = community(communityId, CommunityVisibility.PRIVATE, CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityMemberService.joinCommunity(userId, communityId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("This community requires an approved join request");

        verify(communityMemberRepository, never()).save(any());
    }

    @Test
    void joinCommunityRejectsMissingCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "player"));
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityMemberService.joinCommunity(userId, communityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");
    }

    @Test
    void addMemberRejectsBannedUser() {
        User user = activeUser(UUID.randomUUID(), "player");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        CommunityMember banned = memberWithStatus(community, user, CommunityMemberStatus.BANNED);
        when(communityMemberRepository.findByUserIdAndCommunityId(user.getId(), community.getId()))
                .thenReturn(Optional.of(banned));

        assertThatThrownBy(() -> communityMemberService.addMember(user, community))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is banned from this community");
    }

    @Test
    void addMemberRejectsAlreadyActiveMember() {
        User user = activeUser(UUID.randomUUID(), "player");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        CommunityMember active = memberWithStatus(community, user, CommunityMemberStatus.ACTIVE);
        when(communityMemberRepository.findByUserIdAndCommunityId(user.getId(), community.getId()))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> communityMemberService.addMember(user, community))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is already a member of the community");
    }

    @Test
    void addMemberReactivatesFormerMemberInsteadOfCreatingNewRow() {
        User user = activeUser(UUID.randomUUID(), "player");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        CommunityMember left = memberWithStatus(community, user, CommunityMemberStatus.LEFT);
        left.setRole(CommunityMemberRole.MODERATOR);
        when(communityMemberRepository.findByUserIdAndCommunityId(user.getId(), community.getId()))
                .thenReturn(Optional.of(left));
        when(communityMemberRepository.save(left)).thenReturn(left);

        CommunityMemberResponse response = communityMemberService.addMember(user, community);

        verify(communityMemberRepository).save(left);
        assertThat(left.getStatus()).isEqualTo(CommunityMemberStatus.ACTIVE);
        assertThat(left.getRole()).isEqualTo(CommunityMemberRole.MEMBER);
        assertThat(left.getLeftAt()).isNull();
        assertThat(response.status()).isEqualTo(CommunityMemberStatus.ACTIVE);
    }

    @Test
    void listCommunityMembersReturnsPageForActiveMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = activeUser(userId, "player");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        CommunityMember membership = memberWithStatus(community, user, CommunityMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId))
                .thenReturn(Optional.of(membership));
        when(communityMemberRepository.findByCommunityId(communityId, pageable))
                .thenReturn(new PageImpl<>(List.of(membership), pageable, 1));

        Page<CommunityMemberResponse> response = communityMemberService.listCommunityMembers(communityId, userId, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).extracting(CommunityMemberResponse::memberId).containsExactly(userId);
    }

    @Test
    void listCommunityMembersRejectsNonMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "player"));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityMemberService.listCommunityMembers(communityId, userId, pageable))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not a member of the community");

        verify(communityMemberRepository, never()).findByCommunityId(any(), any());
    }

    @Test
    void leaveCommunityMarksMembershipAsLeftForNonOwner() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User user = activeUser(userId, "player");
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);
        CommunityMember membership = memberWithStatus(community, user, CommunityMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId))
                .thenReturn(Optional.of(membership));

        communityMemberService.leaveCommunity(userId, communityId);

        assertThat(membership.getStatus()).isEqualTo(CommunityMemberStatus.LEFT);
        assertThat(membership.getLeftAt()).isNotNull();
        verify(communityMemberRepository).save(membership);
    }

    @Test
    void leaveCommunityRejectsOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(ownerId, "owner");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);
        CommunityMember membership = memberWithStatus(community, owner, CommunityMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(communityMemberRepository.findByUserIdAndCommunityId(ownerId, communityId))
                .thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> communityMemberService.leaveCommunity(ownerId, communityId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Community owner cannot leave the community");

        verify(communityMemberRepository, never()).save(any());
    }

    @Test
    void leaveCommunityRejectsWhenNotAnActiveMember() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "player"));
        when(communityMemberRepository.findByUserIdAndCommunityId(userId, communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityMemberService.leaveCommunity(userId, communityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User is not a member of the community");
    }

    @Test
    void removeMemberDeletesActiveMemberForOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User owner = activeUser(ownerId, "owner");
        User target = activeUser(memberId, "target");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);
        CommunityMember membership = memberWithStatus(community, target, CommunityMemberStatus.ACTIVE);

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(memberId, communityId))
                .thenReturn(Optional.of(membership));

        communityMemberService.removeMember(ownerId, communityId, memberId);

        assertThat(membership.getStatus()).isEqualTo(CommunityMemberStatus.REMOVED);
        verify(communityMemberRepository).save(membership);
    }

    @Test
    void removeMemberRejectsNonStaffRequester() {
        UUID requesterId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User requester = activeUser(requesterId, "requester");
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);

        when(userFinder.findProfileByUserId(requesterId)).thenReturn(requester);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(requesterId, communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityMemberService.removeMember(requesterId, communityId, memberId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the community owner, an admin or a moderator can remove members");

        verify(communityMemberRepository, never()).save(any());
    }

    @Test
    void removeMemberRejectsSelfRemoval() {
        UUID ownerId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        User owner = activeUser(ownerId, "owner");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> communityMemberService.removeMember(ownerId, communityId, ownerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Use the leave endpoint to remove yourself from the community");
    }

    @Test
    void removeMemberRejectsMissingActiveMembership() {
        UUID ownerId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User owner = activeUser(ownerId, "owner");
        Community community = community(communityId, CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
        when(communityMemberRepository.findByUserIdAndCommunityId(memberId, communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityMemberService.removeMember(ownerId, communityId, memberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member not found in the community");
    }

    @Test
    void isOwnerOrStaffReturnsTrueForOwner() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);

        assertThat(communityMemberService.isOwnerOrStaff(owner, community)).isTrue();
        verify(communityMemberRepository, never()).findByUserIdAndCommunityId(any(), any());
    }

    @Test
    void isOwnerOrStaffReturnsTrueForActiveModerator() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        User moderator = activeUser(UUID.randomUUID(), "mod");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);
        CommunityMember membership = memberWithStatus(community, moderator, CommunityMemberStatus.ACTIVE);
        membership.setRole(CommunityMemberRole.MODERATOR);
        when(communityMemberRepository.findByUserIdAndCommunityId(moderator.getId(), community.getId()))
                .thenReturn(Optional.of(membership));

        assertThat(communityMemberService.isOwnerOrStaff(moderator, community)).isTrue();
    }

    @Test
    void isOwnerOrStaffReturnsFalseForRegularMember() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        User member = activeUser(UUID.randomUUID(), "member");
        Community community = community(UUID.randomUUID(), CommunityVisibility.PUBLIC, CommunityStatus.ACTIVE);
        community.setOwner(owner);
        CommunityMember membership = memberWithStatus(community, member, CommunityMemberStatus.ACTIVE);
        when(communityMemberRepository.findByUserIdAndCommunityId(member.getId(), community.getId()))
                .thenReturn(Optional.of(membership));

        assertThat(communityMemberService.isOwnerOrStaff(member, community)).isFalse();
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

    private CommunityMember memberWithStatus(Community community, User user, CommunityMemberStatus status) {
        Instant now = Instant.now();
        return CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(status)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
