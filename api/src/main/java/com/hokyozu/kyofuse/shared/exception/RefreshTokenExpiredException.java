package com.hokyozu.kyofuse.shared.exception;

/**
 * Exceção lançada quando o refresh token expirou ou foi revogado.
 * Diferencia-se de RefreshTokenAbsentException:
 * - AUSENTE = Cliente não enviou o token (erro do cliente/navegador)
 * - EXPIRADO = Token foi enviado mas está inválido (expiração legítima)
 */
public class RefreshTokenExpiredException extends UnauthorizedException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}

