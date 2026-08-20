package com.hokyozu.kyofuse.reactions.dto.response;

import com.hokyozu.kyofuse.reactions.enums.ReactionType;

import java.util.UUID;

public record PostReactionResponse(
        UUID postId,
        UUID userId,
        String username,
        String nickname,
        String profileImage,
        ReactionType reactionType
) {
}
