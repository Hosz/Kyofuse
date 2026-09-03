package com.hokyozu.kyofuse.leaderboard.service;

import com.hokyozu.kyofuse.leaderboard.dto.response.LeaderboardEntryResponse;
import com.hokyozu.kyofuse.leaderboard.dto.response.UserRankResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final StringRedisTemplate redisTemplate;
    private final GamerProfileFinder gamerProfileFinder;

    public static final String LEADERBOARD_KEY = "leaderboard:premier";

    public void updateScore(UUID userId, double score) {
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, userId.toString(), score);
    }

    public List<LeaderboardEntryResponse> getTopPlayers(int limit) {
        int max = Math.min(Math.max(limit, 1), 100);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD_KEY, 0, max - 1);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = tuples.stream()
                .map(t -> UUID.fromString(Objects.requireNonNull(t.getValue())))
                .toList();

        Map<UUID, GamerProfile> profilesMap = gamerProfileFinder.findAllByUserIds(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        List<LeaderboardEntryResponse> result = new ArrayList<>();
        long rank = 1;
        for (var tuple : tuples) {
            UUID userId = UUID.fromString(Objects.requireNonNull(tuple.getValue()));
            double score = tuple.getScore() != null ? tuple.getScore() : 0.0;
            GamerProfile profile = profilesMap.get(userId);

            result.add(new LeaderboardEntryResponse(
                    rank++,
                    userId,
                    profile != null ? profile.getNickname() : "Unknown",
                    profile != null ? profile.getAvatarUrl() : null,
                    profile != null ? profile.getCountry() : null,
                    (long) score
            ));
        }
        return result;
    }

    public UserRankResponse getUserRank(UUID userId) {
        Long rankZeroBased = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
        Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
        Long totalPlayers = redisTemplate.opsForZSet().zCard(LEADERBOARD_KEY);

        if (rankZeroBased == null || score == null) {
            return new UserRankResponse(userId, null, 0L, totalPlayers != null ? totalPlayers : 0L);
        }

        return new UserRankResponse(userId, rankZeroBased + 1, (long) (double) score, totalPlayers != null ? totalPlayers : 0L);
    }

    public List<LeaderboardEntryResponse> getAroundUser(UUID userId, int range) {
        Long rankZeroBased = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
        if (rankZeroBased == null) {
            return List.of();
        }

        long start = Math.max(0, rankZeroBased - range);
        long end = rankZeroBased + range;

        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD_KEY, start, end);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<UUID> userIds = tuples.stream()
                .map(t -> UUID.fromString(Objects.requireNonNull(t.getValue())))
                .toList();

        Map<UUID, GamerProfile> profilesMap = gamerProfileFinder.findAllByUserIds(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        List<LeaderboardEntryResponse> result = new ArrayList<>();
        long currentRank = start + 1;
        for (var tuple : tuples) {
            UUID id = UUID.fromString(Objects.requireNonNull(tuple.getValue()));
            double score = tuple.getScore() != null ? tuple.getScore() : 0.0;
            GamerProfile profile = profilesMap.get(id);

            result.add(new LeaderboardEntryResponse(
                    currentRank++,
                    id,
                    profile != null ? profile.getNickname() : "Unknown",
                    profile != null ? profile.getAvatarUrl() : null,
                    profile != null ? profile.getCountry() : null,
                    (long) score
            ));
        }
        return result;
    }
}
