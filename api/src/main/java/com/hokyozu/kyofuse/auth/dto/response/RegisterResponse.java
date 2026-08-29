package com.hokyozu.kyofuse.auth.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String username,
        boolean emailVerified,
        String message
) {}
