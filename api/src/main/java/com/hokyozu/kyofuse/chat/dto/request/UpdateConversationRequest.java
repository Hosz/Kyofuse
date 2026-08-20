package com.hokyozu.kyofuse.chat.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateConversationRequest(
        @Size(max = 80)
        String name,

        @Size(max = 500)
        String avatarUrl
) {
}
