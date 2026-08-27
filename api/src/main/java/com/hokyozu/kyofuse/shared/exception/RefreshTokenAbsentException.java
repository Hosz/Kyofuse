package com.hokyozu.kyofuse.shared.exception;

/**
 * Exceção lançada quando o refresh token está ausente na requisição.
 * Diferencia-se de RefreshTokenExpiredException:
 * - AUSENTE = Cliente não enviou o token (erro do cliente/navegador)
 * - EXPIRADO = Token foi enviado mas está inválido (expiração legítima)
 */
public class RefreshTokenAbsentException extends UnauthorizedException {
    public RefreshTokenAbsentException() {
        super("Refresh token ausente.");
    }
}

