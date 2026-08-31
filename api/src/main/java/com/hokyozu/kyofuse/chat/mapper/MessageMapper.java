package com.hokyozu.kyofuse.chat.mapper;

import com.hokyozu.kyofuse.chat.dto.request.MessageRequest;
import com.hokyozu.kyofuse.chat.dto.response.MessageMediaResponse;
import com.hokyozu.kyofuse.chat.dto.response.MessageResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.Message;
import com.hokyozu.kyofuse.chat.entity.MessageMedia;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;

public class MessageMapper {
    public static Message toEntity(Conversation conversation, User user, @Valid MessageRequest request) {
        return Message.builder()
                .conversation(conversation)
                .sender(user)
                .content(request.content() != null ? request.content() : "")
                .createdAt(Instant.now())
                .build();
    }

    public static MessageResponse toResponse(Message message) {
        return toResponse(message, List.of(), null);
    }

    public static MessageResponse toResponse(Message message, GamerProfile senderProfile) {
        return toResponse(message, List.of(), senderProfile);
    }

    public static MessageResponse toResponse(Message message, List<MessageMedia> mediaList, GamerProfile senderProfile) {
        List<MessageMediaResponse> mediaResponses = mediaList == null
                ? List.of()
                : mediaList.stream().map(MessageMediaMapper::toResponse).toList();

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                senderProfile != null ? senderProfile.getNickname() : null,
                senderProfile != null ? senderProfile.getAvatarUrl() : null,
                message.getContent(),
                mediaResponses,
                message.getCreatedAt()
        );
    }
}
