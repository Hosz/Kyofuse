package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteCancelRequest;
import com.hokyozu.kyofuse.communities.dto.request.CommunityInviteRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityInviteResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityInvite;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityInviteRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityInviteServiceTest {

    @Mock
    private CommunityInviteRepository communityInviteRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private CommunityMemberService communityMemberService;

    @Mock
    private UserFinder userFinder;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @Mock
    private NotificationService notificationService;

    @Mock
    private BlockValidator blockValidator;

    @InjectMocks
    private CommunityInviteService communityInviteService;

    @Test
    void inviteUserSuccessfullyCreatesInvite() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        User receiver = testUser(UUID.randomUUID(), "receiver");
        Community community = testCommunity(UUID.randomUUID(), "test-comm", testUser(UUID.randomUUID(), "owner"));

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("receiver")).thenReturn(receiver);
        when(communityMemberService.findCommunityByIdentifier("test-comm")).thenReturn(community);
        when(communityMemberService.isActiveMember(senderId, community.getId())).thenReturn(true);
        when(communityMemberService.isActiveMember(receiver.getId(), community.getId())).thenReturn(false);
        when(communityMemberRepository.findByUserIdAndCommunityId(receiver.getId(), community.getId())).thenReturn(Optional.empty());
        when(communityInviteRepository.existsByCommunityAndReceiverAndStatus(community, receiver, CommunityInviteStatus.PENDING)).thenReturn(false);
        when(communityInviteRepository.save(any(CommunityInvite.class))).thenAnswer(i -> {
            CommunityInvite ci = i.getArgument(0);
            ci.setId(UUID.randomUUID());
            return ci;
        });

        CommunityInviteResponse response = communityInviteService.inviteUser(
                senderId, "test-comm", "receiver", new CommunityInviteRequest("Junte-se a nós!")
        );

        assertThat(response).isNotNull();
        assertThat(response.receiverName()).isEqualTo("receiver");
        assertThat(response.message()).isEqualTo("Junte-se a nós!");
        verify(communityInviteRepository).save(any(CommunityInvite.class));
        verify(notificationService).createNotification(any());
    }

    @Test
    void inviteUserFailsWhenCommunityArchived() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        User receiver = testUser(UUID.randomUUID(), "receiver");
        Community community = testCommunity(UUID.randomUUID(), "archived-comm", testUser(UUID.randomUUID(), "owner"));
        community.setStatus(CommunityStatus.ARCHIVED);

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("receiver")).thenReturn(receiver);
        when(communityMemberService.findCommunityByIdentifier("archived-comm")).thenReturn(community);

        assertThatThrownBy(() -> communityInviteService.inviteUser(senderId, "archived-comm", "receiver", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Não é possível convidar para uma comunidade arquivada.");
    }

    @Test
    void inviteUserFailsWhenSenderNotMember() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        User receiver = testUser(UUID.randomUUID(), "receiver");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("receiver")).thenReturn(receiver);
        when(communityMemberService.findCommunityByIdentifier("comm")).thenReturn(community);
        when(communityMemberService.isActiveMember(senderId, community.getId())).thenReturn(false);

        assertThatThrownBy(() -> communityInviteService.inviteUser(senderId, "comm", "receiver", null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Apenas membros da comunidade podem enviar convites.");
    }

    @Test
    void inviteUserFailsWhenInvitingSelf() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("sender")).thenReturn(sender);
        when(communityMemberService.findCommunityByIdentifier("comm")).thenReturn(community);
        when(communityMemberService.isActiveMember(senderId, community.getId())).thenReturn(true);

        assertThatThrownBy(() -> communityInviteService.inviteUser(senderId, "comm", "sender", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Você não pode convidar a si mesmo.");
    }

    @Test
    void inviteUserFailsWhenInvitingOwner() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        User owner = testUser(UUID.randomUUID(), "owner");
        Community community = testCommunity(UUID.randomUUID(), "comm", owner);

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("owner")).thenReturn(owner);
        when(communityMemberService.findCommunityByIdentifier("comm")).thenReturn(community);
        when(communityMemberService.isActiveMember(senderId, community.getId())).thenReturn(true);

        assertThatThrownBy(() -> communityInviteService.inviteUser(senderId, "comm", "owner", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O usuário já é o dono da comunidade.");
    }

    @Test
    void inviteUserFailsWhenAlreadyMember() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        User receiver = testUser(UUID.randomUUID(), "receiver");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("receiver")).thenReturn(receiver);
        when(communityMemberService.findCommunityByIdentifier("comm")).thenReturn(community);
        when(communityMemberService.isActiveMember(senderId, community.getId())).thenReturn(true);
        when(communityMemberService.isActiveMember(receiver.getId(), community.getId())).thenReturn(true);

        assertThatThrownBy(() -> communityInviteService.inviteUser(senderId, "comm", "receiver", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("O usuário já é membro desta comunidade.");
    }

    @Test
    void inviteUserFailsWhenUserIsBanned() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        User receiver = testUser(UUID.randomUUID(), "receiver");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        CommunityMember bannedMember = CommunityMember.builder()
                .community(community)
                .user(receiver)
                .status(CommunityMemberStatus.BANNED)
                .role(CommunityMemberRole.MEMBER)
                .build();

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(userFinder.findProfileByUsername("receiver")).thenReturn(receiver);
        when(communityMemberService.findCommunityByIdentifier("comm")).thenReturn(community);
        when(communityMemberService.isActiveMember(senderId, community.getId())).thenReturn(true);
        when(communityMemberService.isActiveMember(receiver.getId(), community.getId())).thenReturn(false);
        when(communityMemberRepository.findByUserIdAndCommunityId(receiver.getId(), community.getId())).thenReturn(Optional.of(bannedMember));

        assertThatThrownBy(() -> communityInviteService.inviteUser(senderId, "comm", "receiver", null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("O usuário está banido desta comunidade.");
    }

    @Test
    void acceptInviteSuccessfullyAddsMember() {
        UUID receiverId = UUID.randomUUID();
        User receiver = testUser(receiverId, "receiver");
        User sender = testUser(UUID.randomUUID(), "sender");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        CommunityInvite invite = CommunityInvite.builder()
                .id(UUID.randomUUID())
                .community(community)
                .sender(sender)
                .receiver(receiver)
                .status(CommunityInviteStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(receiverId)).thenReturn(receiver);
        when(communityInviteRepository.findById(invite.getId())).thenReturn(Optional.of(invite));

        communityInviteService.acceptInvite(receiverId, invite.getId());

        assertThat(invite.getStatus()).isEqualTo(CommunityInviteStatus.ACCEPTED);
        assertThat(invite.getRespondedAt()).isNotNull();
        verify(communityMemberService).addMember(receiver, community);
        verify(notificationService).markCommunityInviteAsAccepted(receiverId, invite.getId());
        verify(notificationService).createNotification(any());
    }

    @Test
    void acceptInviteFailsWhenNotReceiver() {
        UUID receiverId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User otherUser = testUser(otherId, "other");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        CommunityInvite invite = CommunityInvite.builder()
                .id(UUID.randomUUID())
                .community(community)
                .sender(testUser(UUID.randomUUID(), "sender"))
                .receiver(testUser(receiverId, "receiver"))
                .status(CommunityInviteStatus.PENDING)
                .build();

        when(userFinder.findProfileByUserId(otherId)).thenReturn(otherUser);
        when(communityInviteRepository.findById(invite.getId())).thenReturn(Optional.of(invite));

        assertThatThrownBy(() -> communityInviteService.acceptInvite(otherId, invite.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Você não tem permissão para aceitar este convite.");
    }

    @Test
    void declineInviteSuccessfullyDeclines() {
        UUID receiverId = UUID.randomUUID();
        User receiver = testUser(receiverId, "receiver");
        User sender = testUser(UUID.randomUUID(), "sender");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        CommunityInvite invite = CommunityInvite.builder()
                .id(UUID.randomUUID())
                .community(community)
                .sender(sender)
                .receiver(receiver)
                .status(CommunityInviteStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(receiverId)).thenReturn(receiver);
        when(communityInviteRepository.findById(invite.getId())).thenReturn(Optional.of(invite));

        communityInviteService.declineInvite(receiverId, invite.getId());

        assertThat(invite.getStatus()).isEqualTo(CommunityInviteStatus.DECLINED);
        assertThat(invite.getRespondedAt()).isNotNull();
        verify(notificationService).markCommunityInviteAsDeclined(receiverId, invite.getId());
        verify(notificationService).createNotification(any());
    }

    @Test
    void cancelInviteSuccessfullyBySender() {
        UUID senderId = UUID.randomUUID();
        User sender = testUser(senderId, "sender");
        Community community = testCommunity(UUID.randomUUID(), "comm", testUser(UUID.randomUUID(), "owner"));

        CommunityInvite invite = CommunityInvite.builder()
                .id(UUID.randomUUID())
                .community(community)
                .sender(sender)
                .receiver(testUser(UUID.randomUUID(), "receiver"))
                .status(CommunityInviteStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(senderId)).thenReturn(sender);
        when(communityInviteRepository.findById(invite.getId())).thenReturn(Optional.of(invite));

        communityInviteService.cancelInvite(senderId, invite.getId(), new CommunityInviteCancelRequest("Cancelado"));

        assertThat(invite.getStatus()).isEqualTo(CommunityInviteStatus.CANCELED);
        assertThat(invite.getCanceledBy()).isEqualTo(sender);
        assertThat(invite.getCancellationReason()).isEqualTo("Cancelado");
        verify(notificationService).markCommunityInviteAsCanceled(invite.getReceiver().getId(), invite.getId());
    }

    private User testUser(UUID id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private Community testCommunity(UUID id, String slug, User owner) {
        return Community.builder()
                .id(id)
                .name("Community " + slug)
                .slug(slug)
                .owner(owner)
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .build();
    }
}
