package com.hokyozu.kyofuse.infrastructure.security.ratelimit;

import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterServiceTest {

    private final RateLimiterService rateLimiterService = new RateLimiterService();

    private static final RateLimitPolicy POLICY = new RateLimitPolicy(
            3,
            Duration.ofMinutes(15),
            List.of(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofSeconds(10))
    );

    @Test
    void allowsAttemptsWithinCapacity() {
        rateLimiterService.checkAndConsume("key-a", POLICY);
        rateLimiterService.checkAndConsume("key-a", POLICY);
        rateLimiterService.checkAndConsume("key-a", POLICY);
        // as 3 tentativas permitidas pela política não devem lançar exceção
    }

    @Test
    void blocksOnceCapacityIsExhaustedAndEscalatesOnRepeatedTrip() {
        exhaust("key-b");

        TooManyAttemptsException firstTrip = catchTooManyAttempts("key-b");
        assertThat(firstTrip.getRetryAfterSeconds()).isLessThanOrEqualTo(1);

        awaitLockoutExpiry(POLICY.lockoutTiers().get(0));

        TooManyAttemptsException secondTrip = catchTooManyAttempts("key-b");
        assertThat(secondTrip.getRetryAfterSeconds()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void rejectsFurtherAttemptsWhileLockedWithoutConsumingMoreTokens() {
        exhaust("key-c");
        catchTooManyAttempts("key-c");

        assertThatThrownBy(() -> rateLimiterService.checkAndConsume("key-c", POLICY))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void recordSuccessResetsBucketAndLockout() {
        exhaust("key-d");
        catchTooManyAttempts("key-d");

        rateLimiterService.recordSuccess("key-d");

        rateLimiterService.checkAndConsume("key-d", POLICY);
        rateLimiterService.checkAndConsume("key-d", POLICY);
        rateLimiterService.checkAndConsume("key-d", POLICY);
        // depois do reset, a política volta a tolerar 3 tentativas antes de bloquear
    }

    @Test
    void differentKeysHaveIndependentBudgets() {
        exhaust("key-e");
        catchTooManyAttempts("key-e");

        rateLimiterService.checkAndConsume("key-f", POLICY);
    }

    private void exhaust(String key) {
        for (int i = 0; i < POLICY.maxAttempts(); i++) {
            rateLimiterService.checkAndConsume(key, POLICY);
        }
    }

    private TooManyAttemptsException catchTooManyAttempts(String key) {
        try {
            rateLimiterService.checkAndConsume(key, POLICY);
        } catch (TooManyAttemptsException exception) {
            return exception;
        }

        throw new AssertionError("Esperava TooManyAttemptsException para a chave " + key);
    }

    private void awaitLockoutExpiry(Duration tier) {
        try {
            Thread.sleep(tier.toMillis() + 50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
