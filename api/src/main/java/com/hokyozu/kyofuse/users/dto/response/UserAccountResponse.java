package com.hokyozu.kyofuse.users.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserAccountResponse(
        UUID id,
        String firstName,
        String lastName,
        String username,
        String email,
        boolean emailVerified,
        boolean isSyntheticEmail,
        boolean hasPassword,
        boolean hasSteam,
        boolean hasGoogle,
        boolean totpEnabled,
        Instant createdAt
) {}
