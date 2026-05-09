package com.hokyozu.kyofuse.auth.dto.response;

import java.util.UUID;

public record AuthResponse(
        String token,
        String tokenType,
        UUID userId,
        String email,
        String username,
        String role
) {
}
