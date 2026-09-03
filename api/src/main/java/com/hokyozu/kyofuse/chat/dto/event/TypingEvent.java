package com.hokyozu.kyofuse.chat.dto.event;

import java.util.UUID;

public record TypingEvent(
        UUID conversationId,
        UUID userId,
        String username,
        String nickname,
        boolean isTyping
) {}
