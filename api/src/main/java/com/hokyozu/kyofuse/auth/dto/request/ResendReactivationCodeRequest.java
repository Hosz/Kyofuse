package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResendReactivationCodeRequest(
        @NotBlank(message = "O token de reativação é obrigatório.")
        String reactivationToken
) {
}
