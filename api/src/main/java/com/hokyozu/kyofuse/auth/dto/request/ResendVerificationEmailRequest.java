package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequest(
        @NotBlank(message = "E-mail ou nome de usuário é obrigatório.")
        String emailOrUsername
) {}
