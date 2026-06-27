package com.hokyozu.kyofuse.reactions.dto.request;

import com.hokyozu.kyofuse.reactions.enums.ReactionType;

public record CommentReactionRequest(
        ReactionType reactionType
) {
}
