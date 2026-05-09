package com.hokyozu.kyofuse.auth.dto.response;

import java.util.UUID;

public record AuthMeResponse(
        UUID userId,
        String email,
        String username,
        String role
) {
}
