package com.hokyozu.kyofuse.comments.dto.response;

import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        String profileImage,
        UUID authorId,
        String authorNickname,
        String authorUsername,
        String content,
        CommentStatus commentStatus,
        Integer reactionCount,
        Integer likeCount,
        ReactionType currentUserReaction,
        Instant createdAt,
        Instant updatedAt
) {
    public CommentResponse(UUID id, UUID postId, String profileImage, UUID authorId, String authorNickname, String authorUsername, String content, CommentStatus commentStatus, Integer reactionCount, Integer likeCount, Instant createdAt, Instant updatedAt) {
        this(id, postId, profileImage, authorId, authorNickname, authorUsername, content, commentStatus, reactionCount, likeCount, null, createdAt, updatedAt);
    }
}
