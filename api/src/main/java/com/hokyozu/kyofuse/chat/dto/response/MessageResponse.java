package com.hokyozu.kyofuse.chat.dto.response;

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
        Instant createdAt
) {
    public MessageResponse(UUID id, UUID conversationId, UUID senderId, String senderUsername, String senderNickname, String senderAvatarUrl, String content, Instant createdAt) {
        this(id, conversationId, senderId, senderUsername, senderNickname, senderAvatarUrl, content, List.of(), createdAt);
    }
}
