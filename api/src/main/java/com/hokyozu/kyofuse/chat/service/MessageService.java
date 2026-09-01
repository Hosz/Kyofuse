package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.event.MessageStatusEvent;
import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageInfoResponse;
import com.hokyozu.kyofuse.chat.dto.response.MessageReceiptItemResponse;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.entity.*;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.chat.enums.MessageStatus;
import com.hokyozu.kyofuse.chat.mapper.MessageMapper;
import com.hokyozu.kyofuse.chat.repository.*;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Duration DELETE_WINDOW = Duration.ofHours(24);

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final GamerProfileFinder gamerProfileFinder;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final MessageRepository messageRepository;
    private final MessageMediaRepository messageMediaRepository;
    private final MessageReceiptRepository messageReceiptRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse sendMessage(UUID conversationId, @Valid MessageRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        validateParticipant(conversation, user);
        validateDirectMessageStatus(conversation, user);

        boolean hasContent = request.content() != null && !request.content().trim().isEmpty();
        boolean hasMedia = request.media() != null && !request.media().isEmpty();
        if (!hasContent && !hasMedia) {
            throw new BadRequestException("Message must contain text content or at least one media attachment");
        }

        Message message = MessageMapper.toEntity(conversation, user, request);
        messageRepository.save(message);

        List<MessageMedia> messageMedias = List.of();
        if (hasMedia) {
            List<MessageMedia> mediaEntities = request.media().stream()
                    .map(item -> MessageMedia.builder()
                            .message(message)
                            .fileKey(item.fileKey())
                            .url(item.url())
                            .thumbnailUrl(item.thumbnailUrl())
                            .contentType(item.contentType())
                            .fileSizeBytes(item.fileSizeBytes())
                            .width(item.width())
                            .height(item.height())
                            .createdAt(Instant.now())
                            .build())
                    .toList();
            messageMedias = messageMediaRepository.saveAll(mediaEntities);
        }

        createReceiptsForRecipients(conversation, message, user);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        MessageResponse response = MessageMapper.toResponse(message, messageMedias, gamerProfileFinder.findProfileByUserId(user.getId()), MessageStatus.SENT);
        notifyRecipients(conversation, user, response);

        return response;
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID conversationId, UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        validateParticipant(conversation, user);

        Page<Message> messages;
        if (conversation.getType() == com.hokyozu.kyofuse.chat.enums.ConversationType.GROUP) {
            ConversationMember member = conversationMemberRepository.findByConversationAndUser(conversation, user)
                    .filter(m -> m.getStatus() == ConversationMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not an active member of this conversation."));
            if (member.getJoinedAt() != null) {
                messages = messageRepository.findByConversationAndCreatedAtGreaterThanEqual(conversation, member.getJoinedAt(), pageable);
            } else {
                messages = messageRepository.findByConversation(conversation, pageable);
            }
        } else {
            messages = messageRepository.findByConversation(conversation, pageable);
        }
        List<UUID> messageIds = messages.getContent().stream().map(Message::getId).toList();

        Map<UUID, List<MessageMedia>> mediaByMessage = messageIds.isEmpty()
                ? Map.of()
                : messageMediaRepository.findByMessageIdIn(messageIds).stream()
                        .collect(Collectors.groupingBy(media -> media.getMessage().getId()));

        Map<UUID, List<MessageReceipt>> receiptsByMessage = messageIds.isEmpty()
                ? Map.of()
                : messageReceiptRepository.findByMessageIdIn(messageIds).stream()
                        .collect(Collectors.groupingBy(receipt -> receipt.getMessage().getId()));

        return messages.map(item -> {
            MessageStatus status = computeMessageStatus(item, userId, receiptsByMessage.getOrDefault(item.getId(), List.of()));
            return MessageMapper.toResponse(
                    item,
                    mediaByMessage.getOrDefault(item.getId(), List.of()),
                    gamerProfileFinder.findProfileByUserId(item.getSender().getId()),
                    status
            );
        });
    }

    @Transactional
    public void deleteMessage(UUID userId, UUID conversationId, UUID messageId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
        Message message = messageRepository.findByIdAndConversation(messageId, conversation)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException("Only the sender can delete this message.");
        }

        if (message.getCreatedAt().isBefore(Instant.now().minus(DELETE_WINDOW))) {
            throw new ForbiddenException("Messages can only be deleted within 24 hours of being sent.");
        }

        messageRepository.delete(message);
    }

    @Transactional
    public void markMessagesAsDelivered(List<UUID> messageIds, UUID recipientId) {
        List<MessageReceipt> receipts = messageReceiptRepository.findByMessageIdInAndUserId(messageIds, recipientId);

        for (MessageReceipt receipt : receipts) {
            if (receipt.getDeliveredAt() == null) {
                receipt.setDeliveredAt(Instant.now());
                receipt.setUpdatedAt(Instant.now());

                notifyStatusUpdate(receipt.getMessage(), MessageStatus.DELIVERED, Instant.now());
            }
        }
        messageReceiptRepository.saveAll(receipts);
    }

    @Transactional
    public void markConversationAsRead(UUID conversationId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        conversationMemberRepository.findByConversationAndUser(conversation, user)
                .ifPresent(member -> {
                    member.setLastReadAt(Instant.now());
                    conversationMemberRepository.save(member);
                });

        List<MessageReceipt> unreadReceipts = messageReceiptRepository.findUnreadByConversationAndUser(conversationId, userId);
        for (MessageReceipt receipt : unreadReceipts) {
            if (receipt.getDeliveredAt() == null) {
                receipt.setDeliveredAt(Instant.now());
            }

            receipt.setReadAt(Instant.now());
            receipt.setUpdatedAt(Instant.now());

            notifyStatusUpdate(receipt.getMessage(), MessageStatus.READ, Instant.now());
        }
        messageReceiptRepository.saveAll(unreadReceipts);
    }

    @Transactional(readOnly = true)
    public MessageInfoResponse getMessageInfo(UUID conversationId, UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        List<MessageReceipt> receipts = messageReceiptRepository.findAllWithUserByMessageId(messageId);

        List<MessageReceiptItemResponse> items = receipts.stream().map(r -> {
            var profile = gamerProfileFinder.findProfileByUserId(r.getUser().getId());
            return new MessageReceiptItemResponse(
                    r.getUser().getId(),
                    r.getUser().getUsername(),
                    profile != null ? profile.getNickname() : r.getUser().getUsername(),
                    profile != null ? profile.getAvatarUrl() : null,
                    r.getDeliveredAt(),
                    r.getReadAt()
            );
        }).toList();

        return new MessageInfoResponse(message.getId(), message.getCreatedAt(), items);
    }

    /**
     * DIRECT: notifica solicitação de mensagem apenas se a conversa for PENDING. Mensagens
     * comuns não geram notificação na área de notificações, apenas são transmitidas via WebSocket.
     * GROUP/COMMUNITY: transmite a mensagem via WebSocket para os membros ativos.
     */
    private void notifyRecipients(Conversation conversation, User sender, MessageResponse response) {
        // Transmite em tempo real para quem estiver com o canal da conversa aberto
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getId(),
                response
        );

        switch (conversation.getType()) {
            case DIRECT -> {
                User recipient = conversation.getDirectUserOne().getId().equals(sender.getId())
                        ? conversation.getDirectUserTwo()
                        : conversation.getDirectUserOne();

                boolean isRequest = conversation.getDirectMessageStatus() == DirectConversationStatus.PENDING;
                if (isRequest) {
                    notificationService.createNotification(
                            CreateNotificationRequest.builder()
                                    .recipient(recipient)
                                    .actor(sender)
                                    .type(NotificationType.MESSAGE_REQUEST)
                                    .title("Solicitação de mensagem")
                                    .message(sender.getUsername() + " quer trocar mensagens com você.")
                                    .targetType(NotificationTargetType.CONVERSATION)
                                    .targetId(conversation.getId())
                                    .build()
                    );
                }

                messagingTemplate.convertAndSendToUser(
                        recipient.getId().toString(),
                        "/queue/messages",
                        response
                );
            }
            case GROUP -> conversationMemberRepository
                    .findByConversationAndStatus(conversation, ConversationMemberStatus.ACTIVE).stream()
                    .map(ConversationMember::getUser)
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .forEach(recipient -> messagingTemplate.convertAndSendToUser(
                            recipient.getId().toString(),
                            "/queue/messages",
                            response
                    ));
            case COMMUNITY -> communityMemberRepository
                    .findByCommunityAndStatus(conversation.getCommunity(), CommunityMemberStatus.ACTIVE).stream()
                    .map(CommunityMember::getUser)
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .forEach(recipient -> messagingTemplate.convertAndSendToUser(
                            recipient.getId().toString(),
                            "/queue/messages",
                            response
                    ));
        }
    }

    /**
     * Conversas GROUP/COMMUNITY não têm direct_message_status (fica null) — regra só
     * se aplica a DIRECT. Enquanto PENDING, apenas quem criou a conversa pode mandar,
     * e só a primeira mensagem: é a "solicitação" descrita no doc.md 4.6, o destinatário
     * só ganha acesso de envio depois de aceitar. Uma vez DECLINED (recusada ou revogada),
     * ninguém pode mandar mais nada.
     */
    private void validateDirectMessageStatus(Conversation conversation, User user) {
        DirectConversationStatus status = conversation.getDirectMessageStatus();
        if (status == null || status == DirectConversationStatus.ACCEPTED) {
            return;
        }

        if (status == DirectConversationStatus.PENDING) {
            boolean isCreator = conversation.getCreatedBy().getId().equals(user.getId());
            boolean alreadyHasMessages = messageRepository.existsByConversation(conversation);
            if (isCreator && !alreadyHasMessages) {
                return;
            }
        }

        throw new ForbiddenException("This conversation is not accepted yet.");
    }

    /**
     * DIRECT: precisa ser um dos dois participantes fixos da conversa.
     * GROUP: precisa de um ConversationMember ACTIVE.
     * COMMUNITY: precisa de um CommunityMember ACTIVE na comunidade vinculada
     * (conversas COMMUNITY não têm ConversationMember próprio — ver ConversationService).
     */
    private void validateParticipant(Conversation conversation, User user) {
        switch (conversation.getType()) {
            case DIRECT -> {
                boolean isParticipant = conversation.getDirectUserOne().getId().equals(user.getId())
                        || conversation.getDirectUserTwo().getId().equals(user.getId());
                if (!isParticipant) {
                    throw new ForbiddenException("User is not a participant in this conversation.");
                }
            }
            case GROUP -> conversationMemberRepository.findByConversationAndUser(conversation, user)
                    .filter(member -> member.getStatus() == ConversationMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not an active member of this conversation."));
            case COMMUNITY -> communityMemberRepository.findByCommunityAndUser(conversation.getCommunity(), user)
                    .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not an active member of this community."));
        }
    }

    private List<User> getConversationRecipients(Conversation conversation, User sender) {
        return switch (conversation.getType()) {
            case DIRECT -> {
                User recipient = conversation.getDirectUserOne().getId().equals(sender.getId())
                        ? conversation.getDirectUserTwo()
                        : conversation.getDirectUserOne();
                yield List.of(recipient);
            }
            case GROUP -> conversationMemberRepository
                    .findByConversationAndStatus(conversation, ConversationMemberStatus.ACTIVE).stream()
                    .map(ConversationMember::getUser)
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .toList();
            case COMMUNITY -> communityMemberRepository
                    .findByCommunityAndStatus(conversation.getCommunity(), CommunityMemberStatus.ACTIVE).stream()
                    .map(CommunityMember::getUser)
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .toList();
        };
    }

    private MessageStatus computeMessageStatus(Message message, UUID currentUserId, List<MessageReceipt> receipts) {
        if (!message.getSender().getId().equals(currentUserId)) {
            return MessageStatus.READ;
        }
        if (receipts.isEmpty()) {
            return MessageStatus.SENT;
        }
        boolean allRead = receipts.stream().allMatch(r -> r.getReadAt() != null);
        if (allRead) {
            return MessageStatus.READ;
        }
        boolean allDelivered = receipts.stream().allMatch(r -> r.getDeliveredAt() != null);
        if (allDelivered) {
            return MessageStatus.DELIVERED;
        }
        return MessageStatus.SENT;
    }

    private void createReceiptsForRecipients(Conversation conversation, Message message, User sender) {
        List<User> recipients = getConversationRecipients(conversation, sender);
        List<MessageReceipt> receipts = recipients.stream()
                .map(recipient -> MessageReceipt.builder()
                        .message(message)
                        .user(recipient)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build())
                .toList();
        messageReceiptRepository.saveAll(receipts);
    }

    private void notifyStatusUpdate(Message message, MessageStatus status, Instant timestamp) {
        MessageStatusEvent event = new MessageStatusEvent(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                status,
                timestamp
        );
        messagingTemplate.convertAndSendToUser(
                message.getSender().getId().toString(),
                "/queue/message-status",
                event
        );
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + message.getConversation().getId() + "/status",
                event
        );
    }
}
