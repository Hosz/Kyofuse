package com.hokyozu.kyofuse.notifications.dto.response;

import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        NotificationStatus status,
        Instant createdAt,
        Instant readAt,
        NotificationActorResponse actor,
        NotificationTargetResponse target,
        Map<String, Object> metadata
) {
}
