package com.hokyozu.kyofuse.chat.dto.response;

import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        ConversationType type,
        String name,
        String avatarUrl,
        UUID createdById,
        String createdByUsername,
        UUID communityId,
        String communityName,
        String communityAvatarUrl,
        UUID directUserOneId,
        String directUserOneUsername,
        String directUserOneNickname,
        String directUserOneAvatarUrl,
        UUID directUserTwoId,
        String directUserTwoUsername,
        String directUserTwoNickname,
        String directUserTwoAvatarUrl,
        DirectConversationStatus directMessageStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
