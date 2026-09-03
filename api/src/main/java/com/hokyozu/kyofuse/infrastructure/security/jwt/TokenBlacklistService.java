package com.hokyozu.kyofuse.infrastructure.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:jwt:";

    public void blacklistToken(String jti, Duration remainingTtl) {
        if (jti != null && !remainingTtl.isNegative() && !remainingTtl.isZero()) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "revoked", remainingTtl);
        }
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
