package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.request.UpdateConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;


public class ConversationMapper {
    public static Conversation toEntityGroup(@Valid ConversationRequest request, User user) {
        return Conversation.builder()
                .type(ConversationType.GROUP)
                .name(request.name())
                .avatarUrl(request.avatarUrl())
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
        return toResponse(conversation, null, null);
    }

    /**
     * Os perfis dos participantes de uma conversa DIRECT são opcionais: quem só precisa
     * do envelope da conversa (aceitar, recusar, revogar) chama a sobrecarga sem eles e
     * os avatares saem nulos. A listagem de conversas passa os dois para o front poder
     * exibir a foto de quem está do outro lado.
     */
    public static ConversationResponse toResponse(Conversation conversation,
                                                  GamerProfile directUserOneProfile,
                                                  GamerProfile directUserTwoProfile) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getAvatarUrl(),
                conversation.getCreatedBy().getId(),
                conversation.getCreatedBy().getUsername(),
                conversation.getCommunity() != null ? conversation.getCommunity().getId() : null,
                conversation.getCommunity() != null ? conversation.getCommunity().getName() : null,
                conversation.getCommunity() != null ? conversation.getCommunity().getAvatarUrl() : null,
                conversation.getDirectUserOne() != null ? conversation.getDirectUserOne().getId() : null,
                conversation.getDirectUserOne() != null ? conversation.getDirectUserOne().getUsername() : null,
                directUserOneProfile != null ? directUserOneProfile.getNickname() : null,
                directUserOneProfile != null ? directUserOneProfile.getAvatarUrl() : null,
                conversation.getDirectUserTwo() != null ? conversation.getDirectUserTwo().getId() : null,
                conversation.getDirectUserTwo() != null ? conversation.getDirectUserTwo().getUsername() : null,
                directUserTwoProfile != null ? directUserTwoProfile.getNickname() : null,
                directUserTwoProfile != null ? directUserTwoProfile.getAvatarUrl() : null,
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

    public static void toEdit(Conversation conversation, @Valid UpdateConversationRequest request) {
        if (request.name() != null) {
            conversation.setName(request.name());
        }

        if (request.avatarUrl() != null) {
            conversation.setAvatarUrl(request.avatarUrl());
        }

        conversation.setUpdatedAt(Instant.now());
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
