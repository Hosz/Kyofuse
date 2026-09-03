package com.hokyozu.kyofuse.auth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.UUID;

@RedisHash(value = "password_reset_tokens", timeToLive = 900)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    private String tokenHash;

    @Indexed
    private UUID userId;

    private Instant createdAt;
}
