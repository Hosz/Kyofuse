package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;

public class MessageMapper {
    public static Message toEntity(Conversation conversation, User user, @Valid MessageRequest request) {
        return Message.builder()
                .conversation(conversation)
                .sender(user)
                .content(request.content())
                .createdAt(Instant.now())
                .build();
    }

    public static MessageResponse toResponse(Message message) {
        return toResponse(message, null);
    }

    /**
     * senderProfile é opcional, mas sem ele o balão da mensagem não tem como exibir a
     * foto de quem enviou — num GROUP isso faria cada mensagem cair no avatar da própria
     * conversa (a foto do grupo) em vez da pessoa.
     */
    public static MessageResponse toResponse(Message message, GamerProfile senderProfile) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                senderProfile != null ? senderProfile.getNickname() : null,
                senderProfile != null ? senderProfile.getAvatarUrl() : null,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
