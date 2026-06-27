package com.hokyozu.kyofuse.reactions.dto.response;

import com.hokyozu.kyofuse.reactions.enums.ReactionType;

import java.util.UUID;

public record PostReactionResponse(
        UUID postId,
        String username,
        ReactionType reactionType
) {
}
