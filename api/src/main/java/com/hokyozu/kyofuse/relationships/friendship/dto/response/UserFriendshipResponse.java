package com.hokyozu.kyofuse.relationships.friendship.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserFriendshipResponse(
        UUID friendId,
        String friendUsername,
        String avatarUrl,
        Instant createdAt
) {
}
