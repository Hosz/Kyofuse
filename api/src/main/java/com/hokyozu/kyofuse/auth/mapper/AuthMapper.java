package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;

import java.time.Instant;

public class AuthMapper {

    private AuthMapper() {}

    public static User toEntity(RegisterRequest request, String passwordHash) {
        Instant now = Instant.now();
        return User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim())
                .username(request.username().trim())
                .passwordHash(passwordHash)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static AuthResponse toResponse(User savedUser, String token) {
        return new AuthResponse(
                token,
                "Bearer",
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );
    }
}

