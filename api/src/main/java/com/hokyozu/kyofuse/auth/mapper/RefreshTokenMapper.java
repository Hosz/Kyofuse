package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.infrastructure.entity.RefreshToken;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;
import java.util.UUID;

public class RefreshTokenMapper {

    private RefreshTokenMapper() {}

    public static RefreshToken toEntity(User user, String tokenHash, UUID familyId, Instant expiresAt) {
        Instant now = Instant.now();
        return RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .familyId(familyId)
                .revoked(false)
                .expiresAt(expiresAt)
                .createdAt(now)
                .build();
    }
}
