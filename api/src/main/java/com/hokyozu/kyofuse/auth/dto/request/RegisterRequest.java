package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

        @NotBlank
        @Size(min = 1, max = 40)
        String username,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
