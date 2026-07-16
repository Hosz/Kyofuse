package com.hokyozu.kyofuse.notifications.dto.request;

import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Builder
public record CreateNotificationRequest(
        User recipient,
        User actor,
        NotificationType type,
        String title,
        String message,
        NotificationTargetType targetType,
        UUID targetId,
        Map<String, Object> metadata
) {
}
