package com.hokyozu.kyofuse.chat.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ConversationRequest(
        @Size(max = 80)
        String name,

        /** Ignorado em conversas DIRECT: só GROUP tem foto própria (ver doc.md 10.6). */
        @Size(max = 500)
        String avatarUrl,

        @NotEmpty
        List<UUID> participantIds
) {
}
