package com.hokyozu.kyofuse.posts.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PostMediaResponse(
        UUID id,
        UUID postId,
        String fileKey,
        String url,
        String thumbnailUrl,
        String contentType,
        Long fileSizeBytes,
        Integer width,
        Integer height,
        Integer displayOrder,
        Instant createdAt
) {
}
