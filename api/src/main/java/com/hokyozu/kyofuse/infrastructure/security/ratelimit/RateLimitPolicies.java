package com.hokyozu.kyofuse.infrastructure.security.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class RateLimitPolicies {

    private final RateLimitPolicy login;
    private final RateLimitPolicy register;

    public RateLimitPolicies(
            @Value("${security.rate-limit.login.max-attempts}") int loginMaxAttempts,
            @Value("${security.rate-limit.login.window-minutes}") long loginWindowMinutes,
            @Value("${security.rate-limit.login.lockout-minutes}") List<Long> loginLockoutMinutes,
            @Value("${security.rate-limit.register.max-attempts}") int registerMaxAttempts,
            @Value("${security.rate-limit.register.window-minutes}") long registerWindowMinutes,
            @Value("${security.rate-limit.register.lockout-minutes}") List<Long> registerLockoutMinutes
    ) {
        this.login = build(loginMaxAttempts, loginWindowMinutes, loginLockoutMinutes);
        this.register = build(registerMaxAttempts, registerWindowMinutes, registerLockoutMinutes);
    }

    public RateLimitPolicy login() {
        return login;
    }

    public RateLimitPolicy register() {
        return register;
    }

    private static RateLimitPolicy build(int maxAttempts, long windowMinutes, List<Long> lockoutMinutes) {
        List<Duration> tiers = lockoutMinutes.stream().map(Duration::ofMinutes).toList();
        return new RateLimitPolicy(maxAttempts, Duration.ofMinutes(windowMinutes), tiers);
    }
}
