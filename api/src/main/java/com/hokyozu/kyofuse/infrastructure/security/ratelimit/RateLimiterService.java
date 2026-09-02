package com.hokyozu.kyofuse.infrastructure.security.ratelimit;

import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Combina duas coisas por chave (ex: "login:ip:1.2.3.4" ou "login:user:hideo"):
 *
 * 1. Um bucket Bucket4j que tolera {@code maxAttempts} tentativas dentro da janela da
 *    política — é isso que permite o burst normal (alguém errando a senha 2-3 vezes).
 * 2. Um bloqueio com duração progressiva: toda vez que o bucket estoura, a chave entra
 *    em lockout por um tempo maior que o anterior (lockoutTiers), até um teto. Um
 *    sucesso (recordSuccess) zera os dois.
 *
 * O estado é local (ConcurrentHashMap) — funciona para uma única instância da API. Pra
 * escalar horizontalmente, troque o Map por buckets obtidos de um ProxyManager
 * distribuído (bucket4j-redis) e o Map de lockouts por algo compartilhado (ex: Redis
 * também); a lógica de tiers abaixo não muda.
 */
@Component
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, LockoutState> lockouts = new ConcurrentHashMap<>();

    private record LockoutState(int tier, Instant lockedUntil) {}

    public void checkAndConsume(String key, RateLimitPolicy policy) {
        LockoutState lockout = lockouts.get(key);

        if (lockout != null && lockout.lockedUntil().isAfter(Instant.now())) {
            throw tooManyAttempts(lockout.lockedUntil());
        }

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(policy));

        if (!bucket.tryConsume(1)) {
            LockoutState escalated = escalate(lockout, policy);
            lockouts.put(key, escalated);
            throw tooManyAttempts(escalated.lockedUntil());
        }
    }

    public void recordSuccess(String key) {
        lockouts.remove(key);

        Bucket bucket = buckets.get(key);
        if (bucket != null) {
            bucket.reset();
        }
    }

    private LockoutState escalate(LockoutState previous, RateLimitPolicy policy) {
        int tier = previous == null ? 0 : Math.min(previous.tier() + 1, policy.lockoutTiers().size() - 1);
        Instant lockedUntil = Instant.now().plus(policy.lockoutTiers().get(tier));

        return new LockoutState(tier, lockedUntil);
    }

    private Bucket newBucket(RateLimitPolicy policy) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(policy.maxAttempts())
                .refillIntervally(policy.maxAttempts(), policy.window())
                .build();

        return Bucket.builder().addLimit(limit).build();
    }

    private TooManyAttemptsException tooManyAttempts(Instant lockedUntil) {
        Duration remaining = Duration.between(Instant.now(), lockedUntil);
        return new TooManyAttemptsException("Muitas tentativas. Tente novamente mais tarde.", remaining);
    }
}
