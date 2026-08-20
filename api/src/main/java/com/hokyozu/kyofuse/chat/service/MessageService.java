package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.chat.mapper.MessageMapper;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.chat.repository.MessageRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

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
    private final NotificationService notificationService;

    @Transactional
    public MessageResponse sendMessage(UUID conversationId, @Valid MessageRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        validateParticipant(conversation, user);
        validateDirectMessageStatus(conversation, user);

        Message message = MessageMapper.toEntity(conversation, user, request);
        messageRepository.save(message);

        notifyRecipients(conversation, user);

        return MessageMapper.toResponse(message, gamerProfileFinder.findProfileByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID conversationId, UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        validateParticipant(conversation, user);

        Page<Message> messages = messageRepository.findByConversation(conversation, pageable);
        return messages.map(item -> MessageMapper.toResponse(
                item,
                gamerProfileFinder.findProfileByUserId(item.getSender().getId())
        ));
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

    /**
     * DIRECT: notifica o outro participante fixo da conversa.
     * GROUP: notifica todo ConversationMember ACTIVE, exceto quem mandou.
     * COMMUNITY: notifica todo CommunityMember ACTIVE da comunidade vinculada, exceto
     * quem mandou (conversas COMMUNITY não têm ConversationMember próprio).
     */
    private void notifyRecipients(Conversation conversation, User sender) {
        switch (conversation.getType()) {
            case DIRECT -> {
                User recipient = conversation.getDirectUserOne().getId().equals(sender.getId())
                        ? conversation.getDirectUserTwo()
                        : conversation.getDirectUserOne();

                // Numa conversa ainda PENDING, essa é a primeira mensagem — ou seja, a
                // solicitação pra trocar mensagens. Notificar como MESSAGE_REQUEST deixa a
                // aba de notificações separar o pedido (que exige uma decisão) de uma
                // mensagem comum, sem gerar duas notificações pro mesmo evento.
                boolean isRequest = conversation.getDirectMessageStatus() == DirectConversationStatus.PENDING;
                notify(recipient, sender, conversation,
                        isRequest ? NotificationType.MESSAGE_REQUEST : NotificationType.NEW_MESSAGE,
                        isRequest ? "Solicitação de mensagem" : "Nova mensagem",
                        isRequest
                                ? sender.getUsername() + " quer trocar mensagens com você."
                                : sender.getUsername() + " te enviou uma mensagem.");
            }
            case GROUP -> conversationMemberRepository
                    .findByConversationAndStatus(conversation, ConversationMemberStatus.ACTIVE).stream()
                    .map(ConversationMember::getUser)
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .forEach(recipient -> notify(recipient, sender, conversation,
                            NotificationType.NEW_MESSAGE, "Nova mensagem",
                            sender.getUsername() + " enviou uma mensagem em " + conversation.getName() + "."));
            case COMMUNITY -> communityMemberRepository
                    .findByCommunityAndStatus(conversation.getCommunity(), CommunityMemberStatus.ACTIVE).stream()
                    .map(CommunityMember::getUser)
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .forEach(recipient -> notify(recipient, sender, conversation,
                            NotificationType.NEW_MESSAGE, "Nova mensagem",
                            sender.getUsername() + " enviou uma mensagem em " + conversation.getCommunity().getName() + "."));
        }
    }

    private void notify(User recipient, User sender, Conversation conversation,
                        NotificationType type, String title, String message) {
        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(recipient)
                        .actor(sender)
                        .type(type)
                        .title(title)
                        .message(message)
                        .targetType(NotificationTargetType.CONVERSATION)
                        .targetId(conversation.getId())
                        .build()
        );
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
}
