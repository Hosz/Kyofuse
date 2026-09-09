package com.hokyozu.kyofuse.leaderboard.service;

import com.hokyozu.kyofuse.leaderboard.dto.response.LeaderboardEntryResponse;
import com.hokyozu.kyofuse.leaderboard.dto.response.UserRankResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final StringRedisTemplate redisTemplate;
    private final GamerProfileFinder gamerProfileFinder;
    private final GamerProfileRepository gamerProfileRepository;

    public static final String LEADERBOARD_KEY = "leaderboard:premier";

    public void updateScore(UUID userId, double score) {
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, userId.toString(), score);
    }

    public void removePlayer(UUID userId) {
        redisTemplate.opsForZSet().remove(LEADERBOARD_KEY, userId.toString());
    }

    public void syncLeaderboardFromDatabase() {
        log.info("[Leaderboard] Sincronizando ranking do Premier a partir do banco de dados...");
        List<GamerProfile> profiles = gamerProfileRepository.findAllWithPremierRatingAndActiveUser();
        if (profiles == null || profiles.isEmpty()) {
            log.info("[Leaderboard] Nenhum jogador ativo com Premier Rating encontrado.");
            return;
        }

        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        for (GamerProfile profile : profiles) {
            if (profile.getUser() != null && profile.getPremierRating() != null) {
                tuples.add(ZSetOperations.TypedTuple.of(
                        profile.getUser().getId().toString(),
                        (double) profile.getPremierRating()
                ));
            }
        }

        if (!tuples.isEmpty()) {
            redisTemplate.opsForZSet().add(LEADERBOARD_KEY, tuples);
            log.info("[Leaderboard] Sincronização concluída: {} jogadores carregados no ranking Redis.", tuples.size());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            syncLeaderboardFromDatabase();
        } catch (Exception e) {
            log.warn("[Leaderboard] Não foi possível sincronizar o ranking com o Redis na inicialização: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopPlayers(int limit) {
        int max = Math.min(Math.max(limit, 1), 100);
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD_KEY, 0, max - 1);

        if ((tuples == null || tuples.isEmpty()) && gamerProfileRepository != null) {
            syncLeaderboardFromDatabase();
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, max - 1);
        }

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
                    profile != null && profile.getUser() != null ? profile.getUser().getUsername() : "Unknown",
                    profile != null ? profile.getNickname() : "Unknown",
                    profile != null ? profile.getAvatarUrl() : null,
                    profile != null ? profile.getCountry() : null,
                    (long) score
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public UserRankResponse getUserRank(UUID userId) {
        Long rankZeroBased = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
        Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
        Long totalPlayers = redisTemplate.opsForZSet().zCard(LEADERBOARD_KEY);

        if (rankZeroBased == null || score == null) {
            if ((totalPlayers == null || totalPlayers == 0) && gamerProfileRepository != null) {
                syncLeaderboardFromDatabase();
                rankZeroBased = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
                score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
                totalPlayers = redisTemplate.opsForZSet().zCard(LEADERBOARD_KEY);
            }
        }

        if (rankZeroBased == null || score == null) {
            return new UserRankResponse(userId, null, 0L, totalPlayers != null ? totalPlayers : 0L);
        }

        return new UserRankResponse(userId, rankZeroBased + 1, (long) (double) score, totalPlayers != null ? totalPlayers : 0L);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getAroundUser(UUID userId, int range) {
        Long rankZeroBased = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
        if (rankZeroBased == null) {
            Long total = redisTemplate.opsForZSet().zCard(LEADERBOARD_KEY);
            if ((total == null || total == 0) && gamerProfileRepository != null) {
                syncLeaderboardFromDatabase();
                rankZeroBased = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
            }
        }

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
                    profile != null && profile.getUser() != null ? profile.getUser().getUsername() : "Unknown",
                    profile != null ? profile.getNickname() : "Unknown",
                    profile != null ? profile.getAvatarUrl() : null,
                    profile != null ? profile.getCountry() : null,
                    (long) score
            ));
        }
        return result;
    }
}
