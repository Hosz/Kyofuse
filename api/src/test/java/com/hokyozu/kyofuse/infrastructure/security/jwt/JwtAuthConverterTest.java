package com.hokyozu.kyofuse.infrastructure.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthConverterTest {

    private final JwtAuthConverter converter = new JwtAuthConverter();

    @Test
    void convertUsesJwtSubjectAsPrincipalNameAndRoleClaimAsAuthority() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "user-id", "role", "USER")
        );

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getName()).isEqualTo("user-id");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }
}
