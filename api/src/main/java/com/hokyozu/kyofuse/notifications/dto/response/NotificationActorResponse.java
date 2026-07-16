package com.hokyozu.kyofuse.notifications.dto.response;

import lombok.Builder;

@Builder
public record NotificationActorResponse(
        String username,
        String avatarUrl
) {
}
