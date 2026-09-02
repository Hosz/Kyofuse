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
}
