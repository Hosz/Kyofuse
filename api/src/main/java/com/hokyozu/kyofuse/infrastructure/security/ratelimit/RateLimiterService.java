package com.hokyozu.kyofuse.infrastructure.security.ratelimit;

import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Rate Limiter distribuído baseado em Redis com bloqueio progressivo por chave:
 *
 * 1. Contador atômico de tentativas (INCR) com TTL na janela configurada.
 * 2. Bloqueio escalonado (lockout tiers) persistido no Redis com TTL automático.
 * 3. Sucesso (recordSuccess) limpa as chaves no Redis.
 */
@Component
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final String ATTEMPTS_PREFIX = "ratelimit:attempts:";
    private static final String LOCKOUT_PREFIX = "ratelimit:lockout:";
    private static final String TIER_PREFIX = "ratelimit:tier:";

    public void checkAndConsume(String key, RateLimitPolicy policy) {
        String lockoutKey = LOCKOUT_PREFIX + key;
        String lockoutVal = redisTemplate.opsForValue().get(lockoutKey);

        if (lockoutVal != null) {
            long lockedUntilEpoch = Long.parseLong(lockoutVal);
            Instant lockedUntil = Instant.ofEpochMilli(lockedUntilEpoch);
            if (lockedUntil.isAfter(Instant.now())) {
                throw tooManyAttempts(lockedUntil);
            }
        }

        String attemptsKey = ATTEMPTS_PREFIX + key;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, policy.window());
        }

        if (attempts != null && attempts > policy.maxAttempts()) {
            Instant lockedUntil = escalateLockout(key, policy);
            throw tooManyAttempts(lockedUntil);
        }
    }

    public void recordSuccess(String key) {
        redisTemplate.delete(List.of(
                ATTEMPTS_PREFIX + key,
                LOCKOUT_PREFIX + key,
                TIER_PREFIX + key
        ));
    }

    private Instant escalateLockout(String key, RateLimitPolicy policy) {
        String tierKey = TIER_PREFIX + key;
        String currentTierVal = redisTemplate.opsForValue().get(tierKey);

        int nextTier = 0;
        if (currentTierVal != null) {
            nextTier = Math.min(Integer.parseInt(currentTierVal) + 1, policy.lockoutTiers().size() - 1);
        }

        Duration lockoutDuration = policy.lockoutTiers().get(nextTier);
        Instant lockedUntil = Instant.now().plus(lockoutDuration);

        redisTemplate.opsForValue().set(LOCKOUT_PREFIX + key, String.valueOf(lockedUntil.toEpochMilli()), lockoutDuration);
        redisTemplate.opsForValue().set(tierKey, String.valueOf(nextTier), Duration.ofHours(24));

        return lockedUntil;
    }

    private TooManyAttemptsException tooManyAttempts(Instant lockedUntil) {
        Duration remaining = Duration.between(Instant.now(), lockedUntil);
        return new TooManyAttemptsException("Muitas tentativas. Tente novamente mais tarde.", remaining);
    }
}
