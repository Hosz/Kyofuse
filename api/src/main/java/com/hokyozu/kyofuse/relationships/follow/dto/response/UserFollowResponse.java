package com.hokyozu.kyofuse.relationships.follow.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserFollowResponse(
        UUID id,
        UUID followerId,
        String followerUsername,
        UUID followedId,
        String followedUsername,
        String status,
        Instant createdAt
) {
}
