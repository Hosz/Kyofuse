package com.hokyozu.kyofuse.chat.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MessageRequest(
        @Size(max = 2000)
        String content,

        List<@Valid MessageMediaItemRequest> media
) {
    public MessageRequest(String content) {
        this(content, List.of());
    }
}
