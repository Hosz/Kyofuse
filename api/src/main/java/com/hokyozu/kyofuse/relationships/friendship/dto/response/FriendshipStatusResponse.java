package com.hokyozu.kyofuse.relationships.friendship.dto.response;

import java.util.UUID;

public record FriendshipStatusResponse(
        boolean isFriend,
        boolean requestSent,
        boolean requestReceived,
        UUID requestId
) {
}
