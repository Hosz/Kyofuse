package com.hokyozu.kyofuse.profiles.dto.response;

import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus;
import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.profiles.enums.Playstyle;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GamerProfileResponse(
        UUID id,
        UUID userId,
        String nickname,
        String bio,
        String avatarUrl,
        String country,
        String city,
        String state,
        PlayerRole mainRole,
        PlayerRole secondaryRole,
        Integer premierRating,
        Integer faceitLevel,
        Integer gcRank,
        Playstyle playstyle,
        Boolean lookingForTeam,
        Boolean lookingForDuo,
        GamerProfileSetupStatus setupStatus,
        List<Cs2Map> favoriteMaps,
        Instant createdAt,
        Instant updatedAt
) {
}
