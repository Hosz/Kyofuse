package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthMeResponse;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.service.AuthService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthCookieService authCookieService;

    @InjectMocks
    private AuthController controller;

    @Test
    void registerDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "john", "password123");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now());
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authService.register(request)).thenReturn(result);
        stubCookies();

        AuthResponse authResponse = controller.register(request, response);

        assertThat(authResponse.userId()).isEqualTo(user.getId());
        assertThat(authResponse.email()).isEqualTo(user.getEmail());
        verify(authService).register(request);
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void loginDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        LoginRequest request = new LoginRequest("john", "password123");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now());
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authService.login(request)).thenReturn(result);
        stubCookies();

        AuthResponse authResponse = controller.login(request, response);

        assertThat(authResponse.userId()).isEqualTo(user.getId());
        verify(authService).login(request);
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void refreshRotatesTokensAndSetsCookies() {
        User user = user();
        AuthService.AuthResult result = new AuthService.AuthResult(user, "new-access-token", "new-refresh-token", Instant.now());
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authService.refresh("old-refresh-token")).thenReturn(result);
        stubCookies();

        AuthResponse authResponse = controller.refresh("old-refresh-token", response);

        assertThat(authResponse.userId()).isEqualTo(user.getId());
        verify(authService).refresh("old-refresh-token");
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void logoutRevokesTokenAndClearsCookies() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authCookieService.buildExpiredAccessTokenCookie())
                .thenReturn(ResponseCookie.from(AuthCookieService.ACCESS_TOKEN_COOKIE, "").maxAge(0).build());
        when(authCookieService.buildExpiredRefreshTokenCookie())
                .thenReturn(ResponseCookie.from(AuthCookieService.REFRESH_TOKEN_COOKIE, "").maxAge(0).build());

        controller.logout("refresh-token", response);

        verify(authService).logout("refresh-token");
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
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

    private void stubCookies() {
        when(authCookieService.buildAccessTokenCookie(any()))
                .thenReturn(ResponseCookie.from(AuthCookieService.ACCESS_TOKEN_COOKIE, "access-token").build());
        when(authCookieService.buildRefreshTokenCookie(any()))
                .thenReturn(ResponseCookie.from(AuthCookieService.REFRESH_TOKEN_COOKIE, "refresh-token").build());
    }

    private static User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .username("john")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
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
