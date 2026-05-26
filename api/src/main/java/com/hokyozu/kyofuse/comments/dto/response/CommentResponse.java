package com.hokyozu.kyofuse.comments.dto.response;

import com.hokyozu.kyofuse.comments.enums.CommentStatus;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID authorId,
        String content,
        CommentStatus commentStatus,
        Integer reactionCount,
        Integer likeCount,
        Instant createdAt,
        Instant updatedAt
) {
}
