package com.hokyozu.kyofuse.comments.mapper;

import com.hokyozu.kyofuse.comments.dto.request.CreateCommentRequest;
import com.hokyozu.kyofuse.comments.dto.response.CommentResponse;
import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class CommentMapper {

    public static Comment toEntity(User user, CreateCommentRequest request, Post postId) {
        return Comment.builder()
                .post(postId)
                .author(user)
                .content(request.content())
                .status(CommentStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CommentResponse toResponse(Comment savedComment, String profileImage, String nickname) {
        return new CommentResponse(
                savedComment.getId(),
                savedComment.getPost().getId(),
                profileImage,
                savedComment.getAuthor().getId(),
                nickname,
                savedComment.getAuthor().getUsername(),
                savedComment.getContent(),
                savedComment.getStatus(),
                savedComment.getReactionCount(),
                savedComment.getLikeCount(),
                savedComment.getCreatedAt(),
                savedComment.getUpdatedAt()
        );
    }
}
