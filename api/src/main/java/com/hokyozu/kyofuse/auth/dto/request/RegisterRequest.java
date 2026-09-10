package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(max = 80)
        String firstName,

        @NotBlank
        @Size(max = 120)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 160)
        String email,

        @NotBlank(message = "O username não pode ser vazio.")
        @Size(min = 3, max = 40, message = "O username deve ter entre 3 e 40 caracteres.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$", message = "O username deve conter apenas letras, números e sublinhado (_), e possuir no mínimo uma letra.")
        String username,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
