package com.hokyozu.kyofuse.auth.dto.response;

public record TotpSetupResponse(
        String secret,
        String otpauthUri
) {
}
