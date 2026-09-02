package com.hokyozu.kyofuse.communities.dto.response;

import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;

import java.time.Instant;
import java.util.UUID;

public record CommunityResponse(
        UUID id,
        String communityName,
        String communitySlug,
        String communityDescription,
        String communityAvatarUrl,
        String communityBannerUrl,
        UUID ownerId,
        String ownerUsername,
        UUID teamId,
        String teamSlug,
        String teamName,
        String teamAvatarUrl,
        CommunityVisibility visibility,
        CommunityStatus status,
        //Integer memberCount,
        //Integer postCount,
        Instant createdAt,
        Instant updatedAt
) {
}
