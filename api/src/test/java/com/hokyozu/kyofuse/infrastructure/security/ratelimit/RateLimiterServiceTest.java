package com.hokyozu.kyofuse.infrastructure.security.ratelimit;

import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimiterService rateLimiterService;

    private static final RateLimitPolicy POLICY = new RateLimitPolicy(
            3,
            Duration.ofMinutes(15),
            List.of(Duration.ofSeconds(60), Duration.ofMinutes(5))
    );

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        rateLimiterService = new RateLimiterService(redisTemplate);
    }

    @Test
    void allowsAttemptsWithinCapacity() {
        when(valueOperations.get("ratelimit:lockout:test-key")).thenReturn(null);
        when(valueOperations.increment("ratelimit:attempts:test-key")).thenReturn(1L);

        rateLimiterService.checkAndConsume("test-key", POLICY);

        verify(redisTemplate).expire(eq("ratelimit:attempts:test-key"), eq(POLICY.window()));
    }

    @Test
    void throwsWhenCurrentlyLockedOut() {
        long futureEpoch = Instant.now().plusSeconds(60).toEpochMilli();
        when(valueOperations.get("ratelimit:lockout:locked-key")).thenReturn(String.valueOf(futureEpoch));

        assertThatThrownBy(() -> rateLimiterService.checkAndConsume("locked-key", POLICY))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void blocksAndEscalatesWhenAttemptsExceedCapacity() {
        when(valueOperations.get("ratelimit:lockout:burst-key")).thenReturn(null);
        when(valueOperations.increment("ratelimit:attempts:burst-key")).thenReturn(4L);
        when(valueOperations.get("ratelimit:tier:burst-key")).thenReturn(null);

        assertThatThrownBy(() -> rateLimiterService.checkAndConsume("burst-key", POLICY))
                .isInstanceOf(TooManyAttemptsException.class);

        verify(valueOperations).set(eq("ratelimit:lockout:burst-key"), any(), eq(POLICY.lockoutTiers().get(0)));
        verify(valueOperations).set(eq("ratelimit:tier:burst-key"), eq("0"), eq(Duration.ofHours(24)));
    }

    @Test
    void recordSuccessDeletesRedisKeys() {
        rateLimiterService.recordSuccess("success-key");

        verify(redisTemplate).delete(List.of(
                "ratelimit:attempts:success-key",
                "ratelimit:lockout:success-key",
                "ratelimit:tier:success-key"
        ));
    }
}
