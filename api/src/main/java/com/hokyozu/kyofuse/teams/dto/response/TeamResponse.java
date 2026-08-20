package com.hokyozu.kyofuse.teams.dto.response;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        UUID ownerId,
        String ownerName,
        String name,
        String slug,
        String avatarUrl,
        String bannerUrl,
        String description,
        String region,
        Integer minPremierRating,
        Integer maxPremierRating,
        Integer minFaceitLevel,
        Integer maxFaceitLevel,
        Integer minGcRank,
        Integer maxGcRank,
        TeamStatus status,
        List<PlayerRole> requiredRoles,
        Instant createdAt,
        Instant updatedAt
) {
}
