package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record Disable2faRequest(

        @NotBlank
        String password
) {
}
