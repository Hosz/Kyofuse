package com.hokyozu.kyofuse.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MessageReceiptItemResponse(
        UUID userId,
        String username,
        String nickname,
        String avatarUrl,
        Instant deliveredAt,
        Instant readAt
) {
}
