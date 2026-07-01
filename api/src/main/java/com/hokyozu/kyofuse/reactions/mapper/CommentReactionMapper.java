package com.hokyozu.kyofuse.reactions.mapper;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.reactions.dto.request.CommentReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.CommentReactionResponse;
import com.hokyozu.kyofuse.reactions.entity.CommentReaction;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.validation.Valid;

import java.time.Instant;

public class CommentReactionMapper {
    public static CommentReaction toEntity(Comment comment, User user, @Valid CommentReactionRequest request) {
        return CommentReaction.builder()
                .comment(comment)
                .user(user)
                .reactionType(request.reactionType())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CommentReactionResponse toResponse(CommentReaction commentReactionSaved) {
        return new CommentReactionResponse(
                commentReactionSaved.getComment().getPost().getId(),
                commentReactionSaved.getComment().getId(),
                commentReactionSaved.getUser().getUsername(),
                commentReactionSaved.getReactionType()
        );
    }
}
