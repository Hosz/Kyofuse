package com.hokyozu.kyofuse.chat.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ConversationRequest(
        @Size(max = 80)
        String name,

        @NotEmpty
        List<UUID> participantIds
) {
}
