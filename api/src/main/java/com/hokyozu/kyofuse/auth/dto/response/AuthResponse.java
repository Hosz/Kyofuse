package com.hokyozu.kyofuse.auth.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String username,
        String role,
        String switchToken
) {
    public AuthResponse(UUID userId, String email, String username, String role) {
        this(userId, email, username, role, null);
    }
}
