package com.hokyozu.kyofuse.chat.dto.event;

import com.hokyozu.kyofuse.chat.enums.MessageStatus;

import java.time.Instant;
import java.util.UUID;

public record MessageStatusEvent(
        UUID messageId,
        UUID conversationId,
        UUID userId,
        MessageStatus status,
        Instant timestamp
) {
}
