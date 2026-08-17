package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.relationships.permission.service.message.MessagePermissionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private MessagePermissionService messagePermissionService;

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private ConversationService conversationService;

    @Test
    void createConversationBuildsGroupWithAdminAndDedupesCreatorFromParticipants() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User creator = activeUser(userId, "creator");
        User other = activeUser(otherId, "other");
        // participantIds inclui o próprio criador por engano — não deve virar uma
        // segunda linha de ConversationMember pra ele.
        ConversationRequest request = new ConversationRequest("Squad", List.of(userId, otherId));

        when(userFinder.findProfileByUserId(userId)).thenReturn(creator);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(conversationMemberRepository.save(any(ConversationMember.class))).thenAnswer(inv -> inv.getArgument(0));

        ConversationResponse response = conversationService.createConversation(request, userId);

        assertThat(response.type()).isEqualTo(ConversationType.GROUP);
        assertThat(response.name()).isEqualTo("Squad");

        ArgumentCaptor<ConversationMember> memberCaptor = ArgumentCaptor.forClass(ConversationMember.class);
        verify(conversationMemberRepository, org.mockito.Mockito.times(2)).save(memberCaptor.capture());
        List<ConversationMember> savedMembers = memberCaptor.getAllValues();
        assertThat(savedMembers.get(0).getRole()).isEqualTo(ConversationMemberRole.ADMIN);
        assertThat(savedMembers.get(0).getUser()).isEqualTo(creator);
        assertThat(savedMembers.get(1).getRole()).isEqualTo(ConversationMemberRole.MEMBER);
        assertThat(savedMembers.get(1).getUser()).isEqualTo(other);

        verify(blockValidator).validate(creator, other);
        verify(blockValidator, never()).validate(creator, creator);
    }

    @Test
    void createConversationRejectsBlockedParticipantInGroup() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User creator = activeUser(userId, "creator");
        User other = activeUser(otherId, "other");
        ConversationRequest request = new ConversationRequest("Squad", List.of(otherId, UUID.randomUUID()));
        User third = activeUser(request.participantIds().get(1), "third");

        when(userFinder.findProfileByUserId(userId)).thenReturn(creator);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(userFinder.findProfileByUserId(request.participantIds().get(1))).thenReturn(third);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));
        org.mockito.Mockito.doThrow(new ForbiddenException("User is blocked by the profile owner."))
                .when(blockValidator).validate(creator, other);

        assertThatThrownBy(() -> conversationService.createConversation(request, userId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createConversationReturnsExistingDirectConversationInsteadOfDuplicating() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User user = activeUser(userId, "alice");
        User other = activeUser(otherId, "bob");
        ConversationRequest request = new ConversationRequest(null, List.of(otherId));
        Conversation existing = directConversation(user, other, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(conversationRepository.findByDirectUserOneAndDirectUserTwo(any(), any())).thenReturn(Optional.of(existing));

        ConversationResponse response = conversationService.createConversation(request, userId);

        assertThat(response.id()).isEqualTo(existing.getId());
        verify(conversationRepository, never()).save(any());
        verify(messagePermissionService, never()).requiresApprovalForFirstMessage(any(), any());
    }

    @Test
    void createConversationOrdersDirectUsersByStringUuidRegardlessOfWhoInitiates() {
        // Monta dois UUIDs onde a ordenação por UUID.compareTo() nativo do Java
        // divergiria da ordenação por String — garante que o service usa String.
        UUID smaller = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID larger = UUID.fromString("80000000-0000-0000-0000-000000000001");
        User initiator = activeUser(larger, "initiator");
        User target = activeUser(smaller, "target");
        ConversationRequest request = new ConversationRequest(null, List.of(smaller));

        when(userFinder.findProfileByUserId(larger)).thenReturn(initiator);
        when(userFinder.findProfileByUserId(smaller)).thenReturn(target);
        when(conversationRepository.findByDirectUserOneAndDirectUserTwo(target, initiator)).thenReturn(Optional.empty());
        when(messagePermissionService.requiresApprovalForFirstMessage(initiator, target)).thenReturn(false);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConversationResponse response = conversationService.createConversation(request, larger);

        assertThat(response.directUserOneId()).isEqualTo(smaller);
        assertThat(response.directUserTwoId()).isEqualTo(larger);
        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.ACCEPTED);
    }

    @Test
    void createConversationCreatesPendingDirectConversationWhenApprovalRequired() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User user = activeUser(userId, "alice");
        User other = activeUser(otherId, "bob");
        ConversationRequest request = new ConversationRequest(null, List.of(otherId));

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(conversationRepository.findByDirectUserOneAndDirectUserTwo(any(), any())).thenReturn(Optional.empty());
        when(messagePermissionService.requiresApprovalForFirstMessage(user, other)).thenReturn(true);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConversationResponse response = conversationService.createConversation(request, userId);

        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.PENDING);
    }

    @Test
    void createConversationOrdersDirectUsersByStringUuidWhenInitiatorIsSmaller() {
        UUID smaller = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID larger = UUID.fromString("80000000-0000-0000-0000-000000000001");
        User initiator = activeUser(smaller, "initiator");
        User target = activeUser(larger, "target");
        ConversationRequest request = new ConversationRequest(null, List.of(larger));

        when(userFinder.findProfileByUserId(smaller)).thenReturn(initiator);
        when(userFinder.findProfileByUserId(larger)).thenReturn(target);
        when(conversationRepository.findByDirectUserOneAndDirectUserTwo(initiator, target)).thenReturn(Optional.empty());
        when(messagePermissionService.requiresApprovalForFirstMessage(initiator, target)).thenReturn(false);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConversationResponse response = conversationService.createConversation(request, smaller);

        assertThat(response.directUserOneId()).isEqualTo(smaller);
        assertThat(response.directUserTwoId()).isEqualTo(larger);
    }

    @Test
    void createCommunityConversationCreatesWhenNoneExistsYet() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = community(owner);

        when(conversationRepository.findByCommunity(community)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> inv.getArgument(0));

        conversationService.createCommunityConversation(community, owner);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(ConversationType.COMMUNITY);
        assertThat(captor.getValue().getCommunity()).isEqualTo(community);
        assertThat(captor.getValue().getName()).isNull();
    }

    @Test
    void createCommunityConversationRejectsWhenOneAlreadyExists() {
        User owner = activeUser(UUID.randomUUID(), "owner");
        Community community = community(owner);
        Conversation existing = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.COMMUNITY)
                .community(community)
                .createdBy(owner)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationRepository.findByCommunity(community)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> conversationService.createCommunityConversation(community, owner))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Conversation for this community already exists.");

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void acceptDirectConversationMarksPendingConversationAsAcceptedForRecipient() {
        User requester = activeUser(UUID.randomUUID(), "requester");
        User recipient = activeUser(UUID.randomUUID(), "recipient");
        Conversation conversation = directConversation(requester, recipient, DirectConversationStatus.PENDING);

        when(userFinder.findProfileByUserId(recipient.getId())).thenReturn(recipient);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        ConversationResponse response = conversationService.acceptDirectConversation(conversation.getId(), recipient.getId());

        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.ACCEPTED);
        assertThat(conversation.getDirectMessageStatus()).isEqualTo(DirectConversationStatus.ACCEPTED);
        verify(conversationRepository).save(conversation);
    }

    @Test
    void declineDirectConversationMarksPendingConversationAsDeclinedForRecipient() {
        User requester = activeUser(UUID.randomUUID(), "requester");
        User recipient = activeUser(UUID.randomUUID(), "recipient");
        Conversation conversation = directConversation(requester, recipient, DirectConversationStatus.PENDING);

        when(userFinder.findProfileByUserId(recipient.getId())).thenReturn(recipient);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        ConversationResponse response = conversationService.declineDirectConversation(conversation.getId(), recipient.getId());

        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.DECLINED);
    }

    @Test
    void acceptDirectConversationRejectsTheRequesterItself() {
        User requester = activeUser(UUID.randomUUID(), "requester");
        User recipient = activeUser(UUID.randomUUID(), "recipient");
        Conversation conversation = directConversation(requester, recipient, DirectConversationStatus.PENDING);

        when(userFinder.findProfileByUserId(requester.getId())).thenReturn(requester);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.acceptDirectConversation(conversation.getId(), requester.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the recipient of the first message can respond to this request.");

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void acceptDirectConversationRejectsNonParticipant() {
        User requester = activeUser(UUID.randomUUID(), "requester");
        User recipient = activeUser(UUID.randomUUID(), "recipient");
        User stranger = activeUser(UUID.randomUUID(), "stranger");
        Conversation conversation = directConversation(requester, recipient, DirectConversationStatus.PENDING);

        when(userFinder.findProfileByUserId(stranger.getId())).thenReturn(stranger);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.acceptDirectConversation(conversation.getId(), stranger.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("User is not a participant in this conversation.");
    }

    @Test
    void acceptDirectConversationRejectsAlreadyAcceptedConversation() {
        User requester = activeUser(UUID.randomUUID(), "requester");
        User recipient = activeUser(UUID.randomUUID(), "recipient");
        Conversation conversation = directConversation(requester, recipient, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(recipient.getId())).thenReturn(recipient);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.acceptDirectConversation(conversation.getId(), recipient.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("This conversation is not pending approval.");
    }

    @Test
    void acceptDirectConversationRejectsNonDirectConversation() {
        User creator = activeUser(UUID.randomUUID(), "creator");
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(creator)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(creator.getId())).thenReturn(creator);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.acceptDirectConversation(conversation.getId(), creator.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only DIRECT conversations support this action.");
    }

    @Test
    void acceptDirectConversationRejectsMissingConversation() {
        User user = activeUser(UUID.randomUUID(), "alice");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.acceptDirectConversation(conversationId, user.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void revokeDirectConversationPermissionMarksAcceptedConversationAsDeclined() {
        User userOne = activeUser(UUID.randomUUID(), "alice");
        User userTwo = activeUser(UUID.randomUUID(), "bob");
        Conversation conversation = directConversation(userOne, userTwo, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(userOne.getId())).thenReturn(userOne);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        ConversationResponse response = conversationService.revokeDirectConversationPermission(conversation.getId(), userOne.getId());

        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.DECLINED);
        verify(conversationRepository).save(conversation);
    }

    @Test
    void revokeDirectConversationPermissionAllowsEitherParticipantNotJustTheOriginalRecipient() {
        // created_by === userOne (quem mandou a 1a mensagem); revoke não tem a
        // restrição de "só quem não é created_by" que accept/decline têm.
        User userOne = activeUser(UUID.randomUUID(), "requester");
        User userTwo = activeUser(UUID.randomUUID(), "recipient");
        Conversation conversation = directConversation(userOne, userTwo, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(userOne.getId())).thenReturn(userOne);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(conversation)).thenReturn(conversation);

        ConversationResponse response = conversationService.revokeDirectConversationPermission(conversation.getId(), userOne.getId());

        assertThat(response.directMessageStatus()).isEqualTo(DirectConversationStatus.DECLINED);
    }

    @Test
    void revokeDirectConversationPermissionRejectsPendingConversation() {
        User userOne = activeUser(UUID.randomUUID(), "alice");
        User userTwo = activeUser(UUID.randomUUID(), "bob");
        Conversation conversation = directConversation(userOne, userTwo, DirectConversationStatus.PENDING);

        when(userFinder.findProfileByUserId(userOne.getId())).thenReturn(userOne);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.revokeDirectConversationPermission(conversation.getId(), userOne.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("This conversation is not currently accepted.");

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void revokeDirectConversationPermissionRejectsNonParticipant() {
        User userOne = activeUser(UUID.randomUUID(), "alice");
        User userTwo = activeUser(UUID.randomUUID(), "bob");
        User stranger = activeUser(UUID.randomUUID(), "stranger");
        Conversation conversation = directConversation(userOne, userTwo, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(stranger.getId())).thenReturn(stranger);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.revokeDirectConversationPermission(conversation.getId(), stranger.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void revokeDirectConversationPermissionRejectsNonDirectConversation() {
        User creator = activeUser(UUID.randomUUID(), "creator");
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(creator)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(creator.getId())).thenReturn(creator);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.revokeDirectConversationPermission(conversation.getId(), creator.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only DIRECT conversations support this action.");
    }

    @Test
    void revokeDirectConversationPermissionRejectsMissingConversation() {
        User user = activeUser(UUID.randomUUID(), "alice");
        UUID conversationId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.revokeDirectConversationPermission(conversationId, user.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listDirectConversationsReturnsMappedPage() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = activeUser(userId, "alice");
        User other = activeUser(UUID.randomUUID(), "bob");
        Conversation conversation = directConversation(user, other, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findAllByTypeAndDirectUser(ConversationType.DIRECT, user, pageable))
                .thenReturn(new PageImpl<>(List.of(conversation), pageable, 1));

        Page<ConversationResponse> response = conversationService.listDirectConversations(userId, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).id()).isEqualTo(conversation.getId());
    }

    @Test
    void listCommunityConversationsExcludesArchivedCommunities() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = activeUser(userId, "alice");
        Community activeCommunity = community(user);
        Community archivedCommunity = community(user);
        archivedCommunity.setStatus(CommunityStatus.ARCHIVED);
        CommunityMember activeMembership = communityMembership(user, activeCommunity);
        CommunityMember archivedMembership = communityMembership(user, archivedCommunity);
        Conversation activeConversation = communityConversation(activeCommunity, user);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityMemberRepository.findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(activeMembership, archivedMembership), pageable, 2));
        when(conversationRepository.findByCommunity(activeCommunity)).thenReturn(Optional.of(activeConversation));

        Page<ConversationResponse> response = conversationService.listCommunityConversations(userId, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).communityId()).isEqualTo(activeCommunity.getId());
        verify(conversationRepository, never()).findByCommunity(archivedCommunity);
    }

    @Test
    void listCommunityConversationsRejectsWhenConversationIsMissingForActiveCommunity() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = activeUser(userId, "alice");
        Community activeCommunity = community(user);
        CommunityMember activeMembership = communityMembership(user, activeCommunity);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityMemberRepository.findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(activeMembership), pageable, 1));
        when(conversationRepository.findByCommunity(activeCommunity)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.listCommunityConversations(userId, pageable))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listGroupConversationsExcludesNonGroupConversations() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        User user = activeUser(userId, "alice");
        Conversation group = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Conversation nonGroup = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.COMMUNITY)
                .createdBy(user)
                .community(community(user))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ConversationMember groupMembership = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(group)
                .user(user)
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        // linha defensiva: hoje conversation_members só é criada pra GROUP, mas o
        // filtro por tipo continua protegendo caso isso mude no futuro.
        ConversationMember nonGroupMembership = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(nonGroup)
                .user(user)
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationMemberRepository.findByUserAndStatus(user, ConversationMemberStatus.ACTIVE, pageable))
                .thenReturn(new PageImpl<>(List.of(groupMembership, nonGroupMembership), pageable, 2));

        Page<ConversationResponse> response = conversationService.listGroupConversations(userId, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id()).isEqualTo(group.getId());
    }

    @Test
    void getConversationDetailsAllowsDirectParticipant() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "alice");
        User other = activeUser(UUID.randomUUID(), "bob");
        Conversation conversation = directConversation(user, other, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getConversationDetails(conversation.getId(), userId);

        assertThat(response.id()).isEqualTo(conversation.getId());
    }

    @Test
    void getConversationDetailsRejectsNonParticipantOfDirectConversation() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "stranger");
        Conversation conversation = directConversation(activeUser(UUID.randomUUID(), "alice"), activeUser(UUID.randomUUID(), "bob"), DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> conversationService.getConversationDetails(conversation.getId(), userId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getConversationDetailsRejectsInactiveGroupMember() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "alice");
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ConversationMember leftMembership = ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .user(user)
                .role(ConversationMemberRole.MEMBER)
                .status(ConversationMemberStatus.LEFT)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, user)).thenReturn(Optional.of(leftMembership));

        assertThatThrownBy(() -> conversationService.getConversationDetails(conversation.getId(), userId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getConversationDetailsRejectsNonMemberOfGroup() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "stranger");
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(activeUser(UUID.randomUUID(), "creator"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversationDetails(conversation.getId(), userId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getConversationDetailsRejectsInactiveCommunityMember() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "alice");
        Community community = community(user);
        Conversation conversation = communityConversation(community, user);
        CommunityMember removedMembership = CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(CommunityMemberStatus.REMOVED)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, user)).thenReturn(Optional.of(removedMembership));

        assertThatThrownBy(() -> conversationService.getConversationDetails(conversation.getId(), userId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getConversationDetailsRejectsNonMemberOfCommunity() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "stranger");
        Community community = community(activeUser(UUID.randomUUID(), "owner"));
        Conversation conversation = communityConversation(community, community.getOwner());

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversationDetails(conversation.getId(), userId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getConversationDetailsAllowsActiveCommunityMember() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "alice");
        Community community = community(user);
        Conversation conversation = communityConversation(community, user);
        CommunityMember membership = communityMembership(user, community);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, user)).thenReturn(Optional.of(membership));

        ConversationResponse response = conversationService.getConversationDetails(conversation.getId(), userId);

        assertThat(response.id()).isEqualTo(conversation.getId());
    }

    @Test
    void getConversationDetailsRejectsMissingConversation() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId, "alice"));
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.getConversationDetails(conversationId, userId))
                .isInstanceOf(NotFoundException.class);
    }

    private User activeUser(UUID id, String username) {
        return User.builder().id(id).username(username).status(UserStatus.ACTIVE).build();
    }

    private Conversation directConversation(User a, User b, DirectConversationStatus status) {
        boolean aIsSmaller = a.getId().toString().compareTo(b.getId().toString()) < 0;
        return Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .createdBy(a)
                .directUserOne(aIsSmaller ? a : b)
                .directUserTwo(aIsSmaller ? b : a)
                .directMessageStatus(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Community community(User owner) {
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2-" + UUID.randomUUID())
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private CommunityMember communityMembership(User user, Community community) {
        return CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Conversation communityConversation(Community community, User creator) {
        return Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.COMMUNITY)
                .createdBy(creator)
                .community(community)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
