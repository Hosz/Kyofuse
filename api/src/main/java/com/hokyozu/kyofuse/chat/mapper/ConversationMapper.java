package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;


public class ConversationMapper {
    public static Conversation toEntityGroup(@Valid ConversationRequest request, User user) {
        return Conversation.builder()
                .type(ConversationType.GROUP)
                .name(request.name())
                .createdBy(user)
                .community(null)
                .directUserOne(null)
                .directUserTwo(null)
                .directMessageStatus(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static ConversationResponse toResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getCreatedBy().getId(),
                conversation.getCreatedBy().getUsername(),
                conversation.getCommunity() != null ? conversation.getCommunity().getId() : null,
                conversation.getCommunity() != null ? conversation.getCommunity().getName() : null,
                conversation.getDirectUserOne() != null ? conversation.getDirectUserOne().getId() : null,
                conversation.getDirectUserOne() != null ? conversation.getDirectUserOne().getUsername() : null,
                conversation.getDirectUserTwo() != null ? conversation.getDirectUserTwo().getId() : null,
                conversation.getDirectUserTwo() != null ? conversation.getDirectUserTwo().getUsername() : null,
                conversation.getDirectMessageStatus(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    /**
     * directUserOne/directUserTwo devem ser passados já ordenados pelo chamador
     * (menor UUID, comparado como String — ver ConversationService) para bater
     * com a comparação nativa do Postgres usada na CHECK da migration e garantir
     * no máximo uma conversa DIRECT por par de usuários.
     */
    public static Conversation toEntityDirect(User createdBy, User directUserOne, User directUserTwo, DirectConversationStatus status) {
        return Conversation.builder()
                .type(ConversationType.DIRECT)
                .name(null)
                .createdBy(createdBy)
                .community(null)
                .directUserOne(directUserOne)
                .directUserTwo(directUserTwo)
                .directMessageStatus(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Conversation toEntityCommunity(Community community, User user) {
        return Conversation.builder()
                .type(ConversationType.COMMUNITY)
                .name(null)
                .createdBy(user)
                .community(community)
                .directUserOne(null)
                .directUserTwo(null)
                .directMessageStatus(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
