package com.hokyozu.kyofuse.notifications.dto.response;

import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record NotificationTargetResponse(
        NotificationTargetType type,
        UUID id
) {
}
