package com.hokyozu.kyofuse.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DisconnectAccountRequest(
        @NotNull(message = "O ID do usuário alvo é obrigatório.")
        UUID targetUserId,

        @NotBlank(message = "O identificador do dispositivo é obrigatório.")
        String deviceId
) {
}
