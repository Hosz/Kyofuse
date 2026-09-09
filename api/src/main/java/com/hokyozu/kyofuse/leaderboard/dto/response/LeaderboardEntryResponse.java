package com.hokyozu.kyofuse.leaderboard.dto.response;

import java.util.UUID;

public record LeaderboardEntryResponse(
        long rank,
        UUID userId,
        String username,
        String nickname,
        String avatarUrl,
        String country,
        long score
) {
    public LeaderboardEntryResponse(long rank, UUID userId, String nickname, String avatarUrl, String country, long score) {
        this(rank, userId, nickname, nickname, avatarUrl, country, score);
    }
}
