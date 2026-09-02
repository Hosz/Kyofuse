package com.hokyozu.kyofuse.infrastructure.security.totp;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.util.UUID;

@RedisHash(value = "mfa_sessions", timeToLive = 300)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaSession {

    @Id
    private String mfaToken;

    private UUID userId;

    @Builder.Default
    private int remainingAttempts = 3;

    private Instant createdAt;
}
