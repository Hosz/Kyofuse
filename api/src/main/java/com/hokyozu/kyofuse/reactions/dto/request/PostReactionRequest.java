package com.hokyozu.kyofuse.reactions.dto.request;

import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import jakarta.validation.constraints.NotNull;

public record PostReactionRequest(

        @NotNull(message = "É necessária uma reação")
        ReactionType reactionType
) {
}
