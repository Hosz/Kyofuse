package com.hokyozu.kyofuse.relationships.friendship.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserFriendRequestResponse(
        UUID senderId,
        String senderUsername,
        UUID receiverId,
        String receiverUsername,
        Instant createdAt
) {
}
