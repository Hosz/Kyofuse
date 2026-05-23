package com.hokyozu.kyofuse.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import static org.assertj.core.api.Assertions.assertThat;

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
        JwtDecoder decoder = securityConfig.jwtDecoder(secret);

        assertThat(encoder).isNotNull();
        assertThat(decoder).isNotNull();
    }
}
