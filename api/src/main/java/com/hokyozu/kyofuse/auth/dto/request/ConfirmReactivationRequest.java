package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmReactivationRequest(
        @NotBlank(message = "O token de reativação é obrigatório.")
        String reactivationToken,

        @NotBlank(message = "O código de verificação é obrigatório.")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter exatamente 6 dígitos numéricos.")
        String code
) {
}
