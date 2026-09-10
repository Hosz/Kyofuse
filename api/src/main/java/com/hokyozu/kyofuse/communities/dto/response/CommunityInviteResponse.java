package com.hokyozu.kyofuse.communities.dto.response;

import com.hokyozu.kyofuse.communities.enums.CommunityInviteStatus;

import java.time.Instant;
import java.util.UUID;

public record CommunityInviteResponse(
        UUID id,
        UUID communityId,
        String communityName,
        String communitySlug,
        String communityAvatarUrl,
        UUID senderId,
        String senderName,
        UUID receiverId,
        String receiverName,
        CommunityInviteStatus status,
        String message,
        Instant createdAt
) {
}
