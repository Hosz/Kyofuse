package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(

        @NotBlank
        @Size(max = 160)
        String emailOrUsername
) {
}
