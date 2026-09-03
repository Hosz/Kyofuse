package com.hokyozu.kyofuse.leaderboard.dto.response;

import java.util.UUID;

public record UserRankResponse(
        UUID userId,
        Long rank,
        long score,
        long totalPlayers
) {}
