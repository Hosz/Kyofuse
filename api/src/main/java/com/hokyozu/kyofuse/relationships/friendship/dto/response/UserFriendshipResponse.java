package com.hokyozu.kyofuse.relationships.friendship.dto.response;

import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;

import java.time.Instant;
import java.util.UUID;

public record UserFriendshipResponse(
        UUID userOneId,
        String userOneUsername,
        UUID userTwoId,
        String userTwoUsername,
        Instant createdAt
) {
    public static UserFriendshipResponse toResponse(UserFriendship friendship) {
        return null;
    }
}
