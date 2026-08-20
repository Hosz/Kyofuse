package com.hokyozu.kyofuse.relationships.block.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserBlockResponse(
        String status,
        UUID blockerId,
        String blockerUsername,
        UUID blockedId,
        String blockedUsername,
        String blockedNickname,
        String blockedAvatarUrl,
        Instant createdAt
) {
}
