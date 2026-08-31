package com.hokyozu.kyofuse.users.dto.request;

public record ScheduleDeletionRequest(
        String password,
        String reason
) {
}
