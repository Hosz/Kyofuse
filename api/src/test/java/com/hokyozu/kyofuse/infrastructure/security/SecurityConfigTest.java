package com.hokyozu.kyofuse.infrastructure.security;

import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtBlacklistValidator;
import com.hokyozu.kyofuse.infrastructure.security.jwt.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoderUsesBcrypt() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        String hash = passwordEncoder.encode("password123");

        assertThat(passwordEncoder.matches("password123", hash)).isTrue();
        assertThat(hash).startsWith("$2");
    }

    @Test
    void jwtEncoderAndDecoderBeansAreCreatedFromSecret() {
        String secret = "kyofuse-local-development-secret-key-change-me-please-123456789";

        JwtEncoder encoder = securityConfig.jwtEncoder(secret);
        JwtBlacklistValidator validator = new JwtBlacklistValidator(mock(TokenBlacklistService.class));
        JwtDecoder decoder = securityConfig.jwtDecoder(secret, validator);

        assertThat(encoder).isNotNull();
        assertThat(decoder).isNotNull();
    }

    @Test
    void bearerTokenResolverReadsTokenFromAccessTokenCookie() {
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "jwt-value"));

        assertThat(resolver.resolve(request)).isEqualTo("jwt-value");
    }

    @Test
    void bearerTokenResolverReturnsNullWhenCookieMissing() {
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other_cookie", "value"));

        assertThat(resolver.resolve(request)).isNull();
    }

    @Test
    void bearerTokenResolverReturnsNullWhenNoCookiesPresent() {
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();

        assertThat(resolver.resolve(new MockHttpServletRequest())).isNull();
    }

    @Test
    void bearerTokenResolverFallsBackToAuthorizationBearerHeaderWhenNoCookiePresent() {
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-jwt-value");

        assertThat(resolver.resolve(request)).isEqualTo("header-jwt-value");
    }

    @Test
    void bearerTokenResolverReturnsNullForWebSocketEndpoints() {
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();

        MockHttpServletRequest wsRequest = new MockHttpServletRequest();
        wsRequest.setRequestURI("/ws");
        wsRequest.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "jwt-value"));
        assertThat(resolver.resolve(wsRequest)).isNull();

        MockHttpServletRequest wsSubpathRequest = new MockHttpServletRequest();
        wsSubpathRequest.setRequestURI("/ws/info");
        wsSubpathRequest.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "jwt-value"));
        assertThat(resolver.resolve(wsSubpathRequest)).isNull();
    }

    @Test
    void bearerTokenResolverReturnsNullForPublicAuthEndpoints() {
        BearerTokenResolver resolver = securityConfig.bearerTokenResolver();

        for (String path : List.of("/api/auth/steam", "/api/auth/google", "/api/auth/login", "/api/auth/refresh")) {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI(path);
            req.setCookies(new Cookie(AuthCookieService.ACCESS_TOKEN_COOKIE, "expired-jwt-value"));
            assertThat(resolver.resolve(req)).as("Path should be exempted: " + path).isNull();
        }
    }

    @Test
    void corsConfigurationSourceRestrictsOriginsAndRejectsWildcards() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(config.getAllowedOriginPatterns()).isNullOrEmpty();
        assertThat(config.getAllowCredentials()).isTrue();
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains("Authorization", "Content-Type", "X-Device-Id", "X-Requested-With");
        assertThat(config.getExposedHeaders()).contains("Set-Cookie", "Retry-After");
    }
}
