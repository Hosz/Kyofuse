package com.hokyozu.kyofuse.communities.dto.response;

import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;

import java.time.Instant;
import java.util.UUID;

public record CommunityMemberResponse(
        UUID id,
        UUID communityId,
        String communityName,
        String communitySlug,
        UUID memberId,
        String memberUsername,
        CommunityMemberRole role,
        CommunityMemberStatus status,
        Instant joinedAt,
        Instant leftAt,
        Instant createdAt,
        Instant updatedAt
) {
}
