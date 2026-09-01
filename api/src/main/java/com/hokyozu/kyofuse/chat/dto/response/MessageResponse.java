package com.hokyozu.kyofuse.chat.dto.response;

import com.hokyozu.kyofuse.chat.enums.MessageStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String senderUsername,
        String senderNickname,
        String senderAvatarUrl,
        String content,
        List<MessageMediaResponse> media,
        Instant createdAt,
        MessageStatus status
) {
}
