package com.hokyozu.kyofuse.auth.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String username,
        String role,
        String switchToken,
        boolean deviceTrusted
) {
    public AuthResponse(UUID userId, String email, String username, String role) {
        this(userId, email, username, role, null, false);
    }

    public AuthResponse(UUID userId, String email, String username, String role, String switchToken) {
        this(userId, email, username, role, switchToken, false);
    }
}
