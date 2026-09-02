package com.hokyozu.kyofuse.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MessageMediaResponse(
        UUID id,
        UUID messageId,
        String fileKey,
        String url,
        String thumbnailUrl,
        String contentType,
        Long fileSizeBytes,
        Integer width,
        Integer height,
        Instant createdAt
) {
}
