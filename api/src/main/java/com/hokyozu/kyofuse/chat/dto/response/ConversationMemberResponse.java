package com.hokyozu.kyofuse.chat.dto.response;

import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationMemberResponse(
        UUID id,
        UUID conversationId,
        String conversationName,
        UUID userId,
        String username,
        String nickname,
        String avatarUrl,
        ConversationMemberRole role,
        ConversationMemberStatus status,
        Instant joinedAt,
        Instant leftAt,
        Instant lastReadAt,
        Instant createdAt,
        Instant updatedAt
) {
}
