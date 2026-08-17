package com.hokyozu.kyofuse.chat.dto.response;

import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        ConversationType type,
        String name,
        UUID createdById,
        String createdByUsername,
        UUID communityId,
        String communityName,
        UUID directUserOneId,
        String directUserOneUsername,
        UUID directUserTwoId,
        String directUserTwoUsername,
        DirectConversationStatus directMessageStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
