package com.hokyozu.kyofuse.auth.dto.response;

public record MfaRequiredResponse(
        boolean mfaRequired,
        String mfaToken
) {
    public MfaRequiredResponse(String mfaToken) {
        this(true, mfaToken);
    }
}
