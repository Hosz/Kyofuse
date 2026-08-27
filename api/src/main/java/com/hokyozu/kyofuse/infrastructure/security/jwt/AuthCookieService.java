package com.hokyozu.kyofuse.infrastructure.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /**
     * Path global "/" garante que o refresh token seja enviado em TODAS as requisições,
     * inclusive para o endpoint /api/auth/refresh. Anteriormente, path="/api/auth"
     * causava um ciclo infinito quando o token expirava fora desse contexto.
     */
    private static final String REFRESH_TOKEN_PATH = "/";

    @Value("${security.jwt.expiration-minutes}")
    private long accessTokenExpirationMinutes;

    @Value("${security.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Value("${security.cookie.secure}")
    private boolean secureCookie;

    public ResponseCookie buildAccessTokenCookie(String token) {
        return baseCookie(ACCESS_TOKEN_COOKIE, token, "/")
                .maxAge(Duration.ofMinutes(accessTokenExpirationMinutes))
                .build();
    }

    public ResponseCookie buildRefreshTokenCookie(String token) {
        return baseCookie(REFRESH_TOKEN_COOKIE, token, REFRESH_TOKEN_PATH)
                .maxAge(Duration.ofDays(refreshTokenExpirationDays))
                .build();
    }

    public ResponseCookie buildExpiredAccessTokenCookie() {
        return baseCookie(ACCESS_TOKEN_COOKIE, "", "/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie buildExpiredRefreshTokenCookie() {
        return baseCookie(REFRESH_TOKEN_COOKIE, "", REFRESH_TOKEN_PATH)
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path(path);
    }
}
