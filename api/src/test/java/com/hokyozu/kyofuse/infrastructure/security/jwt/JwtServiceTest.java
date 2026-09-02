package com.hokyozu.kyofuse.infrastructure.security.jwt;

import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    @Test
    void generateTokenBuildsExpectedClaimsAndReturnsTokenValue() {
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        JwtService jwtService = new JwtService(jwtEncoder);
        ReflectionTestUtils.setField(jwtService, "expirationMinutes", 120L);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .username("player")
                .role(UserRole.USER)
                .totpEnabled(true)
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
            JwtEncoderParameters parameters = invocation.getArgument(0);

            assertThat(parameters.getJwsHeader().getAlgorithm().getName()).isEqualTo("HS256");
            assertThat(parameters.getClaims().<String>getClaim("iss")).isEqualTo("kyofuse-api");
            assertThat(parameters.getClaims().getSubject()).isEqualTo(user.getId().toString());
            assertThat(parameters.getClaims().<String>getClaim("email")).isEqualTo("user@example.com");
            assertThat(parameters.getClaims().<String>getClaim("username")).isEqualTo("player");
            assertThat(parameters.getClaims().<String>getClaim("role")).isEqualTo("USER");
            assertThat(parameters.getClaims().<Boolean>getClaim("totpEnabled")).isTrue();
            assertThat(Duration.between(
                    parameters.getClaims().getIssuedAt(),
                    parameters.getClaims().getExpiresAt()
            )).isEqualTo(Duration.ofMinutes(120));

            return Jwt.withTokenValue("encoded-token")
                    .header("alg", "HS256")
                    .claims(claims -> claims.putAll(parameters.getClaims().getClaims()))
                    .build();
        });

        String token = jwtService.generateToken(user);

        assertThat(token).isEqualTo("encoded-token");
    }
}
