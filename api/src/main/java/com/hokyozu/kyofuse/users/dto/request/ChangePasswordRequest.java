package com.hokyozu.kyofuse.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        String currentPassword,

        @NotBlank(message = "A nova senha não pode ser vazia.")
        @Size(min = 8, max = 100, message = "A nova senha deve ter no mínimo 8 caracteres.")
        String newPassword
) {}
