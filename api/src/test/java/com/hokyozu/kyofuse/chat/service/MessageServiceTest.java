package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.chat.repository.MessageRepository;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Mock
    private CommunityMemberRepository communityMemberRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendMessagePersistsMessageInAcceptedDirectConversation() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.ACCEPTED);
        MessageRequest request = new MessageRequest("hello there");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = messageService.sendMessage(conversation.getId(), request, sender.getId());

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("hello there");
        assertThat(captor.getValue().getSender()).isEqualTo(sender);
        assertThat(response.content()).isEqualTo("hello there");

        ArgumentCaptor<CreateNotificationRequest> notificationCaptor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createNotification(notificationCaptor.capture());
        CreateNotificationRequest notification = notificationCaptor.getValue();
        assertThat(notification.recipient()).isEqualTo(other);
        assertThat(notification.actor()).isEqualTo(sender);
        assertThat(notification.type()).isEqualTo(NotificationType.NEW_MESSAGE);
        assertThat(notification.targetType()).isEqualTo(NotificationTargetType.CONVERSATION);
        assertThat(notification.targetId()).isEqualTo(conversation.getId());
    }

    @Test
    void sendMessageNotifiesDirectUserOneWhenSenderIsDirectUserTwo() {
        // Garante cobertura das duas direções do ternário em notifyRecipients: aqui o
        // remetente é explicitamente directUserTwo, então o destinatário precisa ser
        // directUserOne (na maioria dos outros testes isso depende da ordem aleatória
        // do UUID.randomUUID(), então só uma direção é exercitada por acaso).
        User directUserOne = activeUser("alice");
        User directUserTwo = activeUser("bob");
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.DIRECT)
                .createdBy(directUserTwo)
                .directUserOne(directUserOne)
                .directUserTwo(directUserTwo)
                .directMessageStatus(DirectConversationStatus.ACCEPTED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        MessageRequest request = new MessageRequest("hey");

        when(userFinder.findProfileByUserId(directUserTwo.getId())).thenReturn(directUserTwo);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        messageService.sendMessage(conversation.getId(), request, directUserTwo.getId());

        ArgumentCaptor<CreateNotificationRequest> notificationCaptor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().recipient()).isEqualTo(directUserOne);
    }

    @Test
    void sendMessageNotifiesAllOtherActiveGroupMembersButNotTheSender() {
        User sender = activeUser("alice");
        User memberOne = activeUser("bob");
        User memberTwo = activeUser("carol");
        Conversation conversation = groupConversation(sender);
        ConversationMember senderMembership = conversationMember(conversation, sender, ConversationMemberStatus.ACTIVE);
        MessageRequest request = new MessageRequest("hi squad");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, sender)).thenReturn(Optional.of(senderMembership));
        when(conversationMemberRepository.findByConversationAndStatus(conversation, ConversationMemberStatus.ACTIVE))
                .thenReturn(List.of(
                        senderMembership,
                        conversationMember(conversation, memberOne, ConversationMemberStatus.ACTIVE),
                        conversationMember(conversation, memberTwo, ConversationMemberStatus.ACTIVE)
                ));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        messageService.sendMessage(conversation.getId(), request, sender.getId());

        ArgumentCaptor<CreateNotificationRequest> notificationCaptor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService, times(2)).createNotification(notificationCaptor.capture());
        List<User> recipients = notificationCaptor.getAllValues().stream().map(CreateNotificationRequest::recipient).toList();
        assertThat(recipients).containsExactlyInAnyOrder(memberOne, memberTwo);
    }

    @Test
    void sendMessageNotifiesAllOtherActiveCommunityMembersButNotTheSender() {
        User sender = activeUser("alice");
        User memberOne = activeUser("bob");
        Community community = community(sender);
        Conversation conversation = communityConversation(community, sender);
        CommunityMember senderMembership = communityMember(community, sender, CommunityMemberStatus.ACTIVE);
        MessageRequest request = new MessageRequest("hi community");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, sender)).thenReturn(Optional.of(senderMembership));
        when(communityMemberRepository.findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE))
                .thenReturn(List.of(
                        senderMembership,
                        communityMember(community, memberOne, CommunityMemberStatus.ACTIVE)
                ));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        messageService.sendMessage(conversation.getId(), request, sender.getId());

        ArgumentCaptor<CreateNotificationRequest> notificationCaptor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createNotification(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().recipient()).isEqualTo(memberOne);
    }

    @Test
    void sendMessagePersistsFirstMessageFromCreatorOnPendingDirectConversation() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.PENDING);
        MessageRequest request = new MessageRequest("are you there?");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.existsByConversation(conversation)).thenReturn(false);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = messageService.sendMessage(conversation.getId(), request, sender.getId());

        assertThat(response.content()).isEqualTo("are you there?");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessageNotifiesFirstMessageOnPendingConversationAsAMessageRequest() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.PENDING);

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.existsByConversation(conversation)).thenReturn(false);
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        messageService.sendMessage(conversation.getId(), new MessageRequest("oi"), sender.getId());

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createNotification(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.MESSAGE_REQUEST);
        assertThat(captor.getValue().recipient()).isEqualTo(other);
        assertThat(captor.getValue().targetId()).isEqualTo(conversation.getId());
    }

    @Test
    void sendMessageNotifiesAcceptedDirectConversationAsANormalMessage() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.ACCEPTED);

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        messageService.sendMessage(conversation.getId(), new MessageRequest("oi"), sender.getId());

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService).createNotification(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.NEW_MESSAGE);
    }

    @Test
    void sendMessageRejectsSecondMessageFromCreatorOnPendingDirectConversation() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.PENDING);
        MessageRequest request = new MessageRequest("still there?");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.existsByConversation(conversation)).thenReturn(true);

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, sender.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("This conversation is not accepted yet.");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageRejectsRecipientOnPendingDirectConversation() {
        User creator = activeUser("alice");
        User recipient = activeUser("bob");
        Conversation conversation = directConversation(creator, recipient, DirectConversationStatus.PENDING);
        MessageRequest request = new MessageRequest("let me in first");

        when(userFinder.findProfileByUserId(recipient.getId())).thenReturn(recipient);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, recipient.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("This conversation is not accepted yet.");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageRejectsDeclinedDirectConversation() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.DECLINED);
        MessageRequest request = new MessageRequest("hey");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, sender.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageRejectsNonParticipantOfDirectConversation() {
        User stranger = activeUser("stranger");
        Conversation conversation = directConversation(activeUser("alice"), activeUser("bob"), DirectConversationStatus.ACCEPTED);
        MessageRequest request = new MessageRequest("hi");

        when(userFinder.findProfileByUserId(stranger.getId())).thenReturn(stranger);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, stranger.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessagePersistsMessageForActiveGroupMember() {
        User sender = activeUser("alice");
        Conversation conversation = groupConversation(sender);
        ConversationMember membership = conversationMember(conversation, sender, ConversationMemberStatus.ACTIVE);
        MessageRequest request = new MessageRequest("hi squad");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, sender)).thenReturn(Optional.of(membership));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = messageService.sendMessage(conversation.getId(), request, sender.getId());

        assertThat(response.content()).isEqualTo("hi squad");
    }

    @Test
    void sendMessageRejectsRemovedGroupMember() {
        User sender = activeUser("alice");
        Conversation conversation = groupConversation(sender);
        ConversationMember membership = conversationMember(conversation, sender, ConversationMemberStatus.REMOVED);
        MessageRequest request = new MessageRequest("let me back in");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.findByConversationAndUser(conversation, sender)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, sender.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessagePersistsMessageForActiveCommunityMember() {
        User sender = activeUser("alice");
        Community community = community(sender);
        Conversation conversation = communityConversation(community, sender);
        CommunityMember membership = communityMember(community, sender, CommunityMemberStatus.ACTIVE);
        MessageRequest request = new MessageRequest("hi community");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, sender)).thenReturn(Optional.of(membership));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        MessageResponse response = messageService.sendMessage(conversation.getId(), request, sender.getId());

        assertThat(response.content()).isEqualTo("hi community");
    }

    @Test
    void sendMessageRejectsNonMemberOfCommunity() {
        User sender = activeUser("alice");
        Community community = community(activeUser("owner"));
        Conversation conversation = communityConversation(community, sender);
        MessageRequest request = new MessageRequest("hi");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, sender)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, sender.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageRejectsInactiveCommunityMember() {
        User sender = activeUser("alice");
        Community community = community(sender);
        Conversation conversation = communityConversation(community, sender);
        CommunityMember membership = communityMember(community, sender, CommunityMemberStatus.REMOVED);
        MessageRequest request = new MessageRequest("let me back in");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(communityMemberRepository.findByCommunityAndUser(community, sender)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> messageService.sendMessage(conversation.getId(), request, sender.getId()))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessageRejectsMissingConversation() {
        User sender = activeUser("alice");
        UUID conversationId = UUID.randomUUID();
        MessageRequest request = new MessageRequest("hi");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage(conversationId, request, sender.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getMessagesReturnsPageForParticipant() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = directConversation(sender, other, DirectConversationStatus.ACCEPTED);
        Pageable pageable = PageRequest.of(0, 10);
        Message message = message(conversation, sender, "hi");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversation(conversation, pageable)).thenReturn(new PageImpl<>(List.of(message), pageable, 1));

        Page<MessageResponse> response = messageService.getMessages(conversation.getId(), sender.getId(), pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getMessagesRejectsNonParticipant() {
        User stranger = activeUser("stranger");
        Conversation conversation = directConversation(activeUser("alice"), activeUser("bob"), DirectConversationStatus.ACCEPTED);
        Pageable pageable = PageRequest.of(0, 10);

        when(userFinder.findProfileByUserId(stranger.getId())).thenReturn(stranger);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> messageService.getMessages(conversation.getId(), stranger.getId(), pageable))
                .isInstanceOf(ForbiddenException.class);

        verify(messageRepository, never()).findByConversation(any(), any());
    }

    @Test
    void getMessagesRejectsMissingConversation() {
        User sender = activeUser("alice");
        UUID conversationId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessages(conversationId, sender.getId(), pageable))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteMessageRemovesRecentMessageFromSender() {
        User sender = activeUser("alice");
        Conversation conversation = groupConversation(sender);
        Message message = message(conversation, sender, "oops");

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByIdAndConversation(message.getId(), conversation)).thenReturn(Optional.of(message));

        messageService.deleteMessage(sender.getId(), conversation.getId(), message.getId());

        verify(messageRepository).delete(message);
    }

    @Test
    void deleteMessageRejectsNonSender() {
        User sender = activeUser("alice");
        User other = activeUser("bob");
        Conversation conversation = groupConversation(sender);
        Message message = message(conversation, sender, "oops");

        when(userFinder.findProfileByUserId(other.getId())).thenReturn(other);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByIdAndConversation(message.getId(), conversation)).thenReturn(Optional.of(message));

        assertThatThrownBy(() -> messageService.deleteMessage(other.getId(), conversation.getId(), message.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only the sender can delete this message.");

        verify(messageRepository, never()).delete(any());
    }

    @Test
    void deleteMessageRejectsMessageOlderThan24Hours() {
        User sender = activeUser("alice");
        Conversation conversation = groupConversation(sender);
        Message oldMessage = Message.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .sender(sender)
                .content("ancient")
                .createdAt(Instant.now().minus(Duration.ofHours(25)))
                .build();

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByIdAndConversation(oldMessage.getId(), conversation)).thenReturn(Optional.of(oldMessage));

        assertThatThrownBy(() -> messageService.deleteMessage(sender.getId(), conversation.getId(), oldMessage.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Messages can only be deleted within 24 hours of being sent.");

        verify(messageRepository, never()).delete(any());
    }

    @Test
    void deleteMessageRejectsMissingConversation() {
        User sender = activeUser("alice");
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.deleteMessage(sender.getId(), conversationId, messageId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteMessageRejectsMissingMessage() {
        User sender = activeUser("alice");
        Conversation conversation = groupConversation(sender);
        UUID messageId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(sender.getId())).thenReturn(sender);
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(messageRepository.findByIdAndConversation(messageId, conversation)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.deleteMessage(sender.getId(), conversation.getId(), messageId))
                .isInstanceOf(NotFoundException.class);
    }

    private User activeUser(String username) {
        return User.builder().id(UUID.randomUUID()).username(username).status(UserStatus.ACTIVE).build();
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

    private Conversation groupConversation(User creator) {
        return Conversation.builder()
                .id(UUID.randomUUID())
                .type(ConversationType.GROUP)
                .name("Squad")
                .createdBy(creator)
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

    private ConversationMember conversationMember(Conversation conversation, User user, ConversationMemberStatus status) {
        return ConversationMember.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .user(user)
                .role(ConversationMemberRole.MEMBER)
                .status(status)
                .joinedAt(Instant.now())
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

    private CommunityMember communityMember(Community community, User user, CommunityMemberStatus status) {
        return CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(status)
                .joinedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Message message(Conversation conversation, User sender, String content) {
        return Message.builder()
                .id(UUID.randomUUID())
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .createdAt(Instant.now())
                .build();
    }
}
