package com.hokyozu.kyofuse.reactions.dto.response;

import com.hokyozu.kyofuse.reactions.enums.ReactionType;

import java.util.UUID;

public record CommentReactionResponse(
        UUID postId,
        UUID commentId,
        String username,
        ReactionType reactionType
) {
}
