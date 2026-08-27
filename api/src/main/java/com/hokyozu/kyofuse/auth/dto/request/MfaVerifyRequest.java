package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(

        @NotBlank
        String mfaToken,

        @NotBlank
        String code
) {
}
