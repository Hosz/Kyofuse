package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.entity.PasswordResetToken;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Duration;
import java.time.Instant;

public class PasswordResetTokenMapper {
    public static PasswordResetToken toEntity(User user, String tokenHash, Duration TOKEN_TTL) {
        return PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(TOKEN_TTL))
                .createdAt(Instant.now())
                .build();
    }
}
