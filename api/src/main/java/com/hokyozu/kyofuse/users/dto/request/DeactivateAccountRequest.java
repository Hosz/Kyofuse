package com.hokyozu.kyofuse.users.dto.request;

public record DeactivateAccountRequest(
        String password,
        String reason
) {
}
