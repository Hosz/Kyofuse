package com.hokyozu.kyofuse.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(
        @NotBlank(message = "O username não pode ser vazio.")
        @Size(min = 3, max = 40, message = "O username deve ter entre 3 e 40 caracteres.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$", message = "O username deve conter apenas letras, números e sublinhado (_), e possuir no mínimo uma letra.")
        String username
) {}
