package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.entity.PasswordResetToken;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class PasswordResetTokenMapper {
    public static PasswordResetToken toEntity(UUID userId, String tokenHash) {
        return PasswordResetToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .createdAt(Instant.now())
                .build();
    }
}
