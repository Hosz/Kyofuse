package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.entity.EmailVerificationToken;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Duration;
import java.time.Instant;

public class EmailVerificationTokenMapper {

    private EmailVerificationTokenMapper() {}

    public static EmailVerificationToken toEntity(User user, String tokenHash, Duration tokenTtl) {
        return EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(tokenTtl))
                .createdAt(Instant.now())
                .build();
    }
}
