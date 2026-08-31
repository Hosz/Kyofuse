package com.hokyozu.kyofuse.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageMediaItemRequest(
        @NotBlank String fileKey,
        @NotBlank String url,
        String thumbnailUrl,
        @NotBlank String contentType,
        @NotNull Long fileSizeBytes,
        Integer width,
        Integer height
) {
}
