package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthMeResponse;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void registerDelegatesToAuthService() {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "john", "password123");
        AuthResponse expected = new AuthResponse("token", "Bearer", userId, "john@example.com", "john", "USER");
        when(authService.register(request)).thenReturn(expected);

        AuthResponse response = controller.register(request);

        assertThat(response).isSameAs(expected);
        verify(authService).register(request);
    }

    @Test
    void loginDelegatesToAuthService() {
        UUID userId = UUID.randomUUID();
        LoginRequest request = new LoginRequest("john", "password123");
        AuthResponse expected = new AuthResponse("token", "Bearer", userId, "john@example.com", "john", "USER");
        when(authService.login(request)).thenReturn(expected);

        AuthResponse response = controller.login(request);

        assertThat(response).isSameAs(expected);
        verify(authService).login(request);
    }

    @Test
    void meBuildsResponseFromJwtClaims() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = jwt(userId, Map.of(
                "email", "john@example.com",
                "username", "john",
                "role", "USER"
        ));

        AuthMeResponse response = controller.me(jwt);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.username()).isEqualTo("john");
        assertThat(response.role()).isEqualTo("USER");
    }

    private static Jwt jwt(UUID userId, Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(jwtClaims -> jwtClaims.putAll(claims))
                .build();
    }
}
