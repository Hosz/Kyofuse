package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.infrastructure.entity.RecoveryCode;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class RecoveryCodeMapper {

    private RecoveryCodeMapper() {}

    public static RecoveryCode toEntity(User user, String codeHash, Instant createdAt) {
        return RecoveryCode.builder()
                .user(user)
                .codeHash(codeHash)
                .createdAt(createdAt)
                .build();
    }
}
