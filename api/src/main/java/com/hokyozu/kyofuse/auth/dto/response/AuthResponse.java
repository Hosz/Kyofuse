package com.hokyozu.kyofuse.auth.dto.response;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String username,
        String role
) {
}
