package com.hokyozu.kyofuse.storage.dto.response;

public record MediaUploadResponse(
        String fileKey,
        String url,
        String thumbnailUrl,
        String contentType,
        Long fileSizeBytes,
        Integer width,
        Integer height
) {
}
