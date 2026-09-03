package com.hokyozu.kyofuse.infrastructure.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitResult isAllowed(String key, long limit, long periodInSeconds) {
        String redisKey = "ratelimit:" + key;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (periodInSeconds * 1000L);

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 1. Remove requisições fora da janela de tempo
        zSetOps.removeRangeByScore(redisKey, 0, windowStart);

        // 2. Conta quantas requisições existem na janela atual
        Long currentCount = zSetOps.zCard(redisKey);
        long count = currentCount != null ? currentCount : 0L;

        if (count >= limit) {
            Set<ZSetOperations.TypedTuple<String>> oldestTuples = zSetOps.rangeWithScores(redisKey, 0, 0);
            long retryAfterSeconds = periodInSeconds;
            if (oldestTuples != null && !oldestTuples.isEmpty()) {
                Double oldestScore = oldestTuples.iterator().next().getScore();
                if (oldestScore != null) {
                    long oldestTime = oldestScore.longValue();
                    long remainingMs = (oldestTime + (periodInSeconds * 1000L)) - now;
                    retryAfterSeconds = Math.max(1, (remainingMs + 999) / 1000);
                }
            }
            return new RateLimitResult(false, count, limit, retryAfterSeconds);
        }

        // 3. Adiciona a requisição atual com score do timestamp em ms
        zSetOps.add(redisKey, now + ":" + UUID.randomUUID().toString().substring(0, 8), now);
        redisTemplate.expire(redisKey, Duration.ofSeconds(periodInSeconds + 10));

        return new RateLimitResult(true, count + 1, limit, 0);
    }

    public record RateLimitResult(boolean allowed, long currentCount, long limit, long retryAfterSeconds) {}
}
