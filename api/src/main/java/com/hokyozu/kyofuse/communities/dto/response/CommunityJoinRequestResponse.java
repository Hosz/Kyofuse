package com.hokyozu.kyofuse.communities.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommunityJoinRequestResponse(
        UUID id,
        UUID communityId,
        String communityName,
        String communitySlug,
        UUID userId,
        String username,
        Instant createdAt
) {
}
