package com.hokyozu.kyofuse.infrastructure.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisRateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RedisRateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void isAllowedReturnsTrueWhenCountBelowLimit() {
        String key = "test:127.0.0.1";
        long limit = 5;
        long period = 60;

        when(zSetOperations.zCard("ratelimit:" + key)).thenReturn(2L);

        RedisRateLimiterService.RateLimitResult result = rateLimiterService.isAllowed(key, limit, period);

        assertThat(result.allowed()).isTrue();
        assertThat(result.currentCount()).isEqualTo(3L);
        assertThat(result.limit()).isEqualTo(limit);
        verify(zSetOperations).removeRangeByScore(eq("ratelimit:" + key), eq(0.0), anyDouble());
        verify(zSetOperations).add(eq("ratelimit:" + key), anyString(), anyDouble());
        verify(redisTemplate).expire(eq("ratelimit:" + key), any(Duration.class));
    }

    @Test
    void isAllowedReturnsFalseWhenCountReachesLimit() {
        String key = "test:127.0.0.1";
        long limit = 5;
        long period = 60;
        long now = Instant.now().toEpochMilli();

        when(zSetOperations.zCard("ratelimit:" + key)).thenReturn(5L);
        ZSetOperations.TypedTuple<String> tuple = new DefaultTypedTuple<>("old", (double) (now - 30000));
        when(zSetOperations.rangeWithScores("ratelimit:" + key, 0, 0)).thenReturn(Set.of(tuple));

        RedisRateLimiterService.RateLimitResult result = rateLimiterService.isAllowed(key, limit, period);

        assertThat(result.allowed()).isFalse();
        assertThat(result.currentCount()).isEqualTo(5L);
        assertThat(result.retryAfterSeconds()).isGreaterThan(0L);
        verify(zSetOperations, never()).add(eq("ratelimit:" + key), anyString(), anyDouble());
    }
}
