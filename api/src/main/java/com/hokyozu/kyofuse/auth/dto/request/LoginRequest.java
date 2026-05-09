package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank
        @Size(max = 120)
        String login,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
