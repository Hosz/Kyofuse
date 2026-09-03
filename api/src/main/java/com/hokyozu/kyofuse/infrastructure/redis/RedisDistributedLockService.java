package com.hokyozu.kyofuse.infrastructure.redis;

import com.hokyozu.kyofuse.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisDistributedLockService {

    private final StringRedisTemplate redisTemplate;

    public static final String LOCK_KEY_PREFIX = "lock:";

    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    public boolean tryAcquire(String key, String lockValue, Duration leaseTime) {
        String redisKey = LOCK_KEY_PREFIX + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(redisKey, lockValue, leaseTime);
        return Boolean.TRUE.equals(success);
    }

    public boolean release(String key, String lockValue) {
        String redisKey = LOCK_KEY_PREFIX + key;
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(redisKey), lockValue);
            return result != null && result == 1L;
        } catch (Exception e) {
            log.warn("Erro ao liberar lock para a chave {}: {}", key, e.getMessage());
            return false;
        }
    }

    public <T> T executeWithLock(String key, Duration leaseTime, Supplier<T> task) {
        String lockValue = UUID.randomUUID().toString();
        if (!tryAcquire(key, lockValue, leaseTime)) {
            throw new ConflictException("Outra operação idêntica está em andamento. Tente novamente em instantes.");
        }

        try {
            return task.get();
        } finally {
            release(key, lockValue);
        }
    }

    public void executeWithLock(String key, Duration leaseTime, Runnable task) {
        executeWithLock(key, leaseTime, () -> {
            task.run();
            return null;
        });
    }
}
