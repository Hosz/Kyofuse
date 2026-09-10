package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.request.GoogleLoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.request.VerifyEmailRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthMeResponse;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.dto.response.RegisterResponse;
import com.hokyozu.kyofuse.auth.service.AuthService;
import com.hokyozu.kyofuse.auth.service.EmailVerificationService;
import com.hokyozu.kyofuse.auth.service.PasswordResetService;
import com.hokyozu.kyofuse.auth.service.TwoFactorAuthService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private AuthCookieService authCookieService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.security.steam.SteamService steamService;

    @Mock
    private com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository gamerProfileRepository;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.client.ClientIpResolver clientIpResolver;

    @Mock
    private com.hokyozu.kyofuse.auth.service.AccountReactivationService accountReactivationService;

    @Mock
    private com.hokyozu.kyofuse.auth.repository.UserRepository userRepository;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.security.totp.MfaTokenService mfaTokenService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.geolocation.GeoLocationService geoLocationService;

    @InjectMocks
    private AuthController controller;

    @Test
    void registerDelegatesToAuthServiceAndReturnsRegisterResponse() {
        User user = user();
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "john", "password123");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("203.0.113.10");

        when(clientIpResolver.resolve(httpRequest)).thenReturn("203.0.113.10");
        when(authService.register(request, "203.0.113.10", null, httpRequest)).thenReturn(user);

        RegisterResponse response = controller.register(request, httpRequest);

        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.emailVerified()).isFalse();
        verify(authService).register(request, "203.0.113.10", null, httpRequest);
    }

    @Test
    void verifyEmailDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        user.setEmailVerified(true);
        VerifyEmailRequest request = new VerifyEmailRequest("valid-token");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now());
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authService.verifyEmail("valid-token")).thenReturn(result);
        stubCookies();

        AuthResponse authResponse = controller.verifyEmail(request, response);

        assertThat(authResponse.userId()).isEqualTo(user.getId());
        verify(authService).verifyEmail("valid-token");
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void loginWithGoogleDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        GoogleLoginRequest request = new GoogleLoginRequest("google-id-token");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now());
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(clientIpResolver.resolve(httpRequest)).thenReturn("203.0.113.10");
        when(authService.loginWithGoogle("google-id-token", "203.0.113.10", null, null))
                .thenReturn(new AuthService.LoginOutcome.Authenticated(result));
        stubCookies();

        ResponseEntity<?> responseEntity = controller.loginWithGoogle(request, httpRequest, response);

        AuthResponse authResponse = (AuthResponse) responseEntity.getBody();
        assertThat(authResponse.userId()).isEqualTo(user.getId());
        verify(authService).loginWithGoogle("google-id-token", "203.0.113.10", null, null);
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void redirectToSteamReturnsFoundWithLocationHeader() {
        when(steamService.buildLoginUrl("http://localhost:4200/auth/steam/callback"))
                .thenReturn("https://steamcommunity.com/openid/login?openid.mode=checkid_setup");

        ResponseEntity<Void> response = controller.redirectToSteam("http://localhost:4200/auth/steam/callback");

        assertThat(response.getStatusCode().value()).isEqualTo(302);
        assertThat(response.getHeaders().getLocation()).hasToString("https://steamcommunity.com/openid/login?openid.mode=checkid_setup");
    }

    @Test
    void loginWithSteamDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        Map<String, String> openIdParams = Map.of("openid.mode", "id_res", "openid.claimed_id", "https://steamcommunity.com/openid/id/76561198012345678");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now());
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(clientIpResolver.resolve(httpRequest)).thenReturn("203.0.113.10");
        when(authService.loginWithSteam(openIdParams, "203.0.113.10", null, null))
                .thenReturn(new AuthService.LoginOutcome.Authenticated(result));
        stubCookies();

        ResponseEntity<?> responseEntity = controller.loginWithSteam(openIdParams, httpRequest, response);

        AuthResponse authResponse = (AuthResponse) responseEntity.getBody();
        assertThat(authResponse.userId()).isEqualTo(user.getId());
        verify(authService).loginWithSteam(openIdParams, "203.0.113.10", null, null);
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void loginDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        LoginRequest request = new LoginRequest("john", "password123");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now());
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(clientIpResolver.resolve(httpRequest)).thenReturn("203.0.113.10");
        when(authService.login(request, "203.0.113.10", null, null)).thenReturn(new AuthService.LoginOutcome.Authenticated(result));
        stubCookies();

        ResponseEntity<?> responseEntity = controller.login(request, httpRequest, response);

        AuthResponse authResponse = (AuthResponse) responseEntity.getBody();
        assertThat(authResponse.userId()).isEqualTo(user.getId());
        verify(authService).login(request, "203.0.113.10", null, null);
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void loginReturnsMfaChallengeWithoutSettingCookiesWhenTotpIsEnabled() {
        LoginRequest request = new LoginRequest("john", "password123");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(clientIpResolver.resolve(httpRequest)).thenReturn("203.0.113.10");
        when(authService.login(request, "203.0.113.10", null, null))
                .thenReturn(new AuthService.LoginOutcome.MfaRequired("mfa-token"));

        ResponseEntity<?> responseEntity = controller.login(request, httpRequest, response);

        assertThat(responseEntity.getBody())
                .isEqualTo(new com.hokyozu.kyofuse.auth.dto.response.MfaRequiredResponse("mfa-token"));
        assertThat(response.getCookies()).isEmpty();
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
                "role", "USER",
                "totpEnabled", true
        ));

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .username("john")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .totpEnabled(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        com.hokyozu.kyofuse.profiles.entity.GamerProfile profile = com.hokyozu.kyofuse.profiles.entity.GamerProfile.builder()
                .setupStatus(com.hokyozu.kyofuse.profiles.enums.GamerProfileSetupStatus.COMPLETED)
                .build();
        when(gamerProfileRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(profile));

        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        AuthMeResponse response = controller.me(jwt, httpResponse);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.username()).isEqualTo("john");
        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.totpEnabled()).isTrue();
        assertThat(response.profileSetupStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void meThrowsWhenUserIsInactiveAndClearsCookies() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = jwt(userId, Map.of("email", "john@example.com"));

        User user = User.builder()
                .id(userId)
                .email("john@example.com")
                .username("john")
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .build();
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(authCookieService.buildExpiredAccessTokenCookie()).thenReturn(ResponseCookie.from(AuthCookieService.ACCESS_TOKEN_COOKIE, "").build());
        when(authCookieService.buildExpiredRefreshTokenCookie()).thenReturn(ResponseCookie.from(AuthCookieService.REFRESH_TOKEN_COOKIE, "").build());

        MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.me(jwt, httpResponse))
                .isInstanceOf(com.hokyozu.kyofuse.shared.exception.UnauthorizedException.class);
    }

    @Test
    void switchAccountDelegatesToAuthServiceAndSetsCookies() {
        User user = user();
        com.hokyozu.kyofuse.auth.dto.request.SwitchAccountRequest request =
                new com.hokyozu.kyofuse.auth.dto.request.SwitchAccountRequest(user.getId(), "switch-token", "device-1");
        AuthService.AuthResult result = new AuthService.AuthResult(user, "access-token", "refresh-token", Instant.now(), "new-switch-token");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(clientIpResolver.resolve(httpRequest)).thenReturn("203.0.113.10");
        when(authService.switchAccount(request, "203.0.113.10", null)).thenReturn(result);
        stubCookies();

        ResponseEntity<com.hokyozu.kyofuse.auth.dto.response.SwitchAccountResponse> responseEntity =
                controller.switchAccount(request, httpRequest, response);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().userId()).isEqualTo(user.getId());
        assertThat(responseEntity.getBody().switchToken()).isEqualTo("new-switch-token");
        assertThat(response.getCookies()).extracting("name")
                .containsExactlyInAnyOrder(AuthCookieService.ACCESS_TOKEN_COOKIE, AuthCookieService.REFRESH_TOKEN_COOKIE);
    }

    @Test
    void disconnectAccountDelegatesToAuthServiceWithAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = jwt(userId, Map.of("email", "john@example.com", "username", "john", "role", "USER"));
        com.hokyozu.kyofuse.auth.dto.request.DisconnectAccountRequest request =
                new com.hokyozu.kyofuse.auth.dto.request.DisconnectAccountRequest(userId, "device-1");

        controller.disconnectAccount(request, jwt);

        verify(authService).disconnectAccount(request, userId);
    }

    @Test
    void getSwitchTokenReturnsSwitchTokenWhenDeviceIdPresent() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = jwt(userId, Map.of("email", "john@example.com", "username", "john", "role", "USER"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Device-Id", "device-123");

        when(authService.generateSwitchToken(userId, "device-123")).thenReturn("generated-switch-token");

        ResponseEntity<Map<String, String>> response = controller.getSwitchToken(jwt, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("switchToken", "generated-switch-token");
    }

    private void stubCookies() {
        when(authCookieService.buildAccessTokenCookie(any()))
                .thenReturn(ResponseCookie.from(AuthCookieService.ACCESS_TOKEN_COOKIE, "access-token").build());
        when(authCookieService.buildRefreshTokenCookie(any()))
                .thenReturn(ResponseCookie.from(AuthCookieService.REFRESH_TOKEN_COOKIE, "refresh-token").build());
    }

    @Test
    void getGoogleClientId_returnsConfiguredClientId() {
        controller.setGoogleClientId("test-client-id.apps.googleusercontent.com");
        ResponseEntity<Map<String, String>> response = controller.getGoogleClientId();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("clientId")).isEqualTo("test-client-id.apps.googleusercontent.com");
    }

    @Test
    void getLocationReturnsCountryAndCityWhenResolved() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientIpResolver.resolve(request)).thenReturn("200.189.1.5");
        when(geoLocationService.resolveLocation(request, "200.189.1.5"))
                .thenReturn(com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo.of("200.189.1.5", "São Paulo", "SP", "Brasil", "BR"));

        ResponseEntity<Map<String, String>> response = controller.getLocation(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("countryCode", "BR");
        assertThat(response.getBody()).containsEntry("country", "Brasil");
        assertThat(response.getBody()).containsEntry("city", "São Paulo");
    }

    private static User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .username("john")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
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
