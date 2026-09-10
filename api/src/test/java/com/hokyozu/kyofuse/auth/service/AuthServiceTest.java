package com.hokyozu.kyofuse.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.validator.EmailAndUsernameAvailabilityValidator;
import com.hokyozu.kyofuse.auth.validator.LoginFinderValidator;
import com.hokyozu.kyofuse.auth.validator.LoginValidator;
import com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenService;
import com.hokyozu.kyofuse.infrastructure.security.oauth.GoogleTokenVerifierService;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.infrastructure.security.totp.MfaTokenService;
import com.hokyozu.kyofuse.infrastructure.security.totp.RecoveryCodeService;
import com.hokyozu.kyofuse.infrastructure.security.totp.TotpService;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import com.hokyozu.kyofuse.relationships.privacy.service.UserPrivacySettingsService;
import com.hokyozu.kyofuse.shared.exception.TooManyAttemptsException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchService accountSwitchService;

    @Mock
    private GamerProfileService gamerProfileService;

    @Mock
    private UserPrivacySettingsService userPrivacySettingsService;

    @Mock
    private EmailAndUsernameAvailabilityValidator emailAndUsernameAvailabilityValidator;

    @Mock
    private LoginFinderValidator loginFinderValidator;

    @Mock
    private LoginValidator loginValidator;

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private RateLimitPolicies rateLimitPolicies;

    @Mock
    private EmailCipherService emailCipherService;

    @Mock
    private MfaTokenService mfaTokenService;

    @Mock
    private TotpService totpService;

    @Mock
    private RecoveryCodeService recoveryCodeService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.security.steam.SteamService steamService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.geolocation.GeoLocationService geoLocationService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.client.UserAgentParser userAgentParser;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Mock
    private AccountReactivationService accountReactivationService;

    @InjectMocks
    private AuthService authService;

    private static final String CLIENT_IP = "203.0.113.10";

    @Test
    void registerCreatesUserProfileAndTriggersVerificationEmail() {
        RegisterRequest request = new RegisterRequest(
                " Hideo ",
                " Kojima ",
                " hideo@example.com ",
                " hideo ",
                "password123"
        );
        UUID userId = UUID.randomUUID();

        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(emailCipherService.blindIndex(" hideo@example.com ")).thenReturn("email-index-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        User result = authService.register(request, CLIENT_IP);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(rateLimiterService).checkAndConsume(eq("register:ip:" + CLIENT_IP), any());
        verify(emailAndUsernameAvailabilityValidator).validate("email-index-hash", " hideo ");
        verify(userRepository).save(userCaptor.capture());
        verify(gamerProfileService).createGamerProfileMin(userCaptor.getValue());
        verify(userPrivacySettingsService).createDefault(userCaptor.getValue());
        verify(emailVerificationService).createVerificationToken(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstName()).isEqualTo("Hideo");
        assertThat(savedUser.getLastName()).isEqualTo("Kojima");
        assertThat(savedUser.getEmail()).isEqualTo("hideo@example.com");
        assertThat(savedUser.getEmailIndex()).isEqualTo("email-index-hash");
        assertThat(savedUser.getUsername()).isEqualTo("hideo");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.isEmailVerified()).isFalse();

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("hideo@example.com");
        assertThat(result.getUsername()).isEqualTo("hideo");
    }

    @Test
    void registerPopulatesRegistrationCountryAndDevice() {
        RegisterRequest request = new RegisterRequest("Sam", "Porter", "sam@example.com", "sam_bridges", "pass12345");
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pwd");
        when(emailCipherService.blindIndex(anyString())).thenReturn("email-idx");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        when(geoLocationService.resolveLocation("203.0.113.10"))
                .thenReturn(LocationInfo.of("203.0.113.10", "Curitiba", "PR", "Brasil", "BR"));
        when(userAgentParser.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64)"))
                .thenReturn(new com.hokyozu.kyofuse.infrastructure.client.DeviceInfo("Chrome", "Windows", "Computador", "Chrome no Windows"));

        User user = authService.register(request, "203.0.113.10", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        assertThat(user.getRegistrationCountry()).isEqualTo("Brasil");
        assertThat(user.getRegistrationCountryCode()).isEqualTo("BR");
        assertThat(user.getRegistrationDevice()).isEqualTo("Chrome no Windows");
    }

    @Test
    void registerIsBlockedWhenIpRateLimitIsExceeded() {
        RegisterRequest request = new RegisterRequest("Hideo", "Kojima", "hideo@example.com", "hideo", "password123");

        doThrow(new TooManyAttemptsException("Muitas tentativas.", Duration.ofMinutes(5)))
                .when(rateLimiterService).checkAndConsume(eq("register:ip:" + CLIENT_IP), any());

        assertThatThrownBy(() -> authService.register(request, CLIENT_IP))
                .isInstanceOf(TooManyAttemptsException.class);

        verifyNoInteractions(emailAndUsernameAvailabilityValidator, userRepository);
    }

    @Test
    void loginValidatesUserAndReturnsTokens() {
        LoginRequest request = new LoginRequest("hideo", "password123");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("hideo@example.com")
                .username("hideo")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Instant refreshExpiresAt = Instant.now().plusSeconds(3600);

        when(loginFinderValidator.validate(request)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.issue(user))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", refreshExpiresAt));

        AuthService.LoginOutcome outcome = authService.login(request, CLIENT_IP);

        verify(loginValidator).validate(user, request);
        verify(rateLimiterService).checkAndConsume(eq("login:ip:" + CLIENT_IP), any());
        verify(rateLimiterService).checkAndConsume(eq("login:user:hideo"), any());
        verify(rateLimiterService).recordSuccess("login:ip:" + CLIENT_IP);
        verify(rateLimiterService).recordSuccess("login:user:hideo");
        verifyNoInteractions(mfaTokenService);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.Authenticated.class);
        AuthService.AuthResult result = ((AuthService.LoginOutcome.Authenticated) outcome).result();
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user().getId()).isEqualTo(user.getId());
        assertThat(result.user().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void loginReturnsReactivationRequiredWhenAccountIsInactive() {
        LoginRequest request = new LoginRequest("hideo", "password123");
        User inactiveUser = User.builder()
                .id(UUID.randomUUID())
                .email("hideo@example.com")
                .username("hideo")
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .deactivatedAt(Instant.now())
                .emailVerified(true)
                .build();

        when(loginFinderValidator.validate(request)).thenReturn(inactiveUser);
        when(accountReactivationService.createAndSendReactivationCode(inactiveUser))
                .thenReturn(new com.hokyozu.kyofuse.auth.dto.response.ReactivationRequiredResponse("react-token", "h***o@example.com", false, null));

        AuthService.LoginOutcome outcome = authService.login(request, CLIENT_IP);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.ReactivationRequired.class);
        AuthService.LoginOutcome.ReactivationRequired react = (AuthService.LoginOutcome.ReactivationRequired) outcome;
        assertThat(react.reactivationToken()).isEqualTo("react-token");
        assertThat(react.maskedEmail()).isEqualTo("h***o@example.com");
    }

    @Test
    void loginWithGoogleAuthenticatesExistingVerifiedUser() {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-123");
        payload.setEmail("hideo@example.com");
        payload.set("given_name", "Hideo");
        payload.set("family_name", "Kojima");

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("hideo@example.com")
                .emailIndex("email-index-hash")
                .username("hideo")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        Instant refreshExpiresAt = Instant.now().plusSeconds(3600);

        when(googleTokenVerifierService.verify("google-token")).thenReturn(payload);
        when(emailCipherService.blindIndex("hideo@example.com")).thenReturn("email-index-hash");
        when(userRepository.findByGoogleId("google-sub-123")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.issue(user))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", refreshExpiresAt));

        AuthService.LoginOutcome outcome = authService.loginWithGoogle("google-token", CLIENT_IP);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.Authenticated.class);
        verify(rateLimiterService).recordSuccess("login:ip:" + CLIENT_IP);
    }

    @Test
    void loginWithGoogleCreatesNewUserWhenNotExists() {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-456");
        payload.setEmail("newuser@gmail.com");
        payload.set("given_name", "New");
        payload.set("family_name", "Gamer");

        when(googleTokenVerifierService.verify("google-token")).thenReturn(payload);
        when(emailCipherService.blindIndex("newuser@gmail.com")).thenReturn("email-index-new");
        when(userRepository.findByGoogleId("google-sub-456")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIndex("email-index-new")).thenReturn(Optional.empty());
        when(userRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("random-hash");
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(refreshTokenService.issue(any()))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", Instant.now().plusSeconds(3600)));

        AuthService.LoginOutcome outcome = authService.loginWithGoogle("google-token", CLIENT_IP);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.Authenticated.class);
        verify(userRepository).save(any(User.class));
        verify(gamerProfileService).createGamerProfileMin(any(User.class));
        verify(userPrivacySettingsService).createDefault(any(User.class));
    }

    @Test
    void loginWithSteamAuthenticatesExistingUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .steamId("76561198012345678")
                .email("steam_76561198012345678@steam.kyofuse.local")
                .username("steamuser")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Map<String, String> openIdParams = Map.of("openid.mode", "id_res");

        when(steamService.validateOpenIdAndGetSteamId(openIdParams)).thenReturn("76561198012345678");
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.issue(user))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", Instant.now().plusSeconds(3600)));

        AuthService.LoginOutcome outcome = authService.loginWithSteam(openIdParams, CLIENT_IP);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.Authenticated.class);
        verify(rateLimiterService).recordSuccess("login:ip:" + CLIENT_IP);
    }

    @Test
    void loginWithSteamCreatesNewUserWhenNotExists() {
        Map<String, String> openIdParams = Map.of("openid.mode", "id_res");
        com.hokyozu.kyofuse.infrastructure.security.steam.SteamPlayerSummary summary =
                new com.hokyozu.kyofuse.infrastructure.security.steam.SteamPlayerSummary(
                        "76561198012345678", "GamerHero", "https://steamcommunity.com/id/gamerhero", "https://avatar.url", "BR"
                );

        when(steamService.validateOpenIdAndGetSteamId(openIdParams)).thenReturn("76561198012345678");
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.empty());
        when(steamService.getPlayerSummary("76561198012345678")).thenReturn(Optional.of(summary));
        when(emailCipherService.blindIndex(anyString())).thenReturn("email-index-steam");
        when(userRepository.existsByUsernameIgnoreCase("gamerhero")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("random-hash");
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(refreshTokenService.issue(any()))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", Instant.now().plusSeconds(3600)));

        AuthService.LoginOutcome outcome = authService.loginWithSteam(openIdParams, CLIENT_IP);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.Authenticated.class);
        verify(userRepository).save(any(User.class));
        verify(gamerProfileService).createGamerProfile(any(User.class), eq("GamerHero"), eq("https://avatar.url"), eq("BR"));
        verify(userPrivacySettingsService).createDefault(any(User.class));
    }

    @Test
    void loginReturnsMfaChallengeWhenTotpIsEnabled() {
        LoginRequest request = new LoginRequest("hideo", "password123");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("hideo@example.com")
                .username("hideo")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .totpEnabled(true)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(loginFinderValidator.validate(request)).thenReturn(user);
        when(mfaTokenService.generate(user)).thenReturn("mfa-token");

        AuthService.LoginOutcome outcome = authService.login(request, CLIENT_IP);

        assertThat(outcome).isInstanceOf(AuthService.LoginOutcome.MfaRequired.class);
        assertThat(((AuthService.LoginOutcome.MfaRequired) outcome).mfaToken()).isEqualTo("mfa-token");
        verify(rateLimiterService).recordSuccess("login:ip:" + CLIENT_IP);
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void verifyMfaIssuesTokensWithValidTotpCode() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("hideo@example.com")
                .username("hideo")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .totpEnabled(true)
                .totpSecret("secret")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Instant refreshExpiresAt = Instant.now().plusSeconds(3600);

        when(mfaTokenService.resolveUserId("mfa-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(totpService.verifyCode("secret", "123456")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.issue(user))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", refreshExpiresAt));

        AuthService.AuthResult result = authService.verifyMfa("mfa-token", "123456");

        verify(rateLimiterService).checkAndConsume(eq("mfa:user:" + userId), any());
        verify(rateLimiterService).recordSuccess("mfa:user:" + userId);
        verifyNoInteractions(recoveryCodeService);
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void verifyMfaFallsBackToRecoveryCodeWhenTotpCodeIsInvalid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("hideo@example.com")
                .username("hideo")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .totpEnabled(true)
                .totpSecret("secret")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        Instant refreshExpiresAt = Instant.now().plusSeconds(3600);

        when(mfaTokenService.resolveUserId("mfa-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(totpService.verifyCode("secret", "ABCDE-12345")).thenReturn(false);
        when(recoveryCodeService.consume(user, "ABCDE-12345")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.issue(user))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", refreshExpiresAt));

        AuthService.AuthResult result = authService.verifyMfa("mfa-token", "ABCDE-12345");

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        verify(rateLimiterService).recordSuccess("mfa:user:" + userId);
    }

    @Test
    void verifyMfaRejectsInvalidCodeAndDoesNotResetRateLimit() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .totpEnabled(true)
                .totpSecret("secret")
                .build();

        when(mfaTokenService.resolveUserId("mfa-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(totpService.verifyCode("secret", "000000")).thenReturn(false);
        when(recoveryCodeService.consume(user, "000000")).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyMfa("mfa-token", "000000"))
                .isInstanceOf(UnauthorizedException.class);

        verify(rateLimiterService, never()).recordSuccess(any());
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void verifyMfaRejectsBlankToken() {
        assertThatThrownBy(() -> authService.verifyMfa(" ", "123456"))
                .isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(mfaTokenService, rateLimiterService);
    }

    @Test
    void verifyMfaRejectsWhenUserNoLongerHasTotpEnabled() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).totpEnabled(false).build();

        when(mfaTokenService.resolveUserId("mfa-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        assertThatThrownBy(() -> authService.verifyMfa("mfa-token", "123456"))
                .isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(totpService, recoveryCodeService, jwtService, refreshTokenService);
    }

    @Test
    void loginIsBlockedWhenRateLimitIsExceededBeforeValidatingCredentials() {
        LoginRequest request = new LoginRequest("hideo", "password123");

        doThrow(new TooManyAttemptsException("Muitas tentativas.", Duration.ofMinutes(1)))
                .when(rateLimiterService).checkAndConsume(eq("login:ip:" + CLIENT_IP), any());

        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(TooManyAttemptsException.class);

        verifyNoInteractions(loginFinderValidator, loginValidator);
    }

    @Test
    void loginDoesNotResetRateLimitWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("hideo", "wrong-password");

        when(loginFinderValidator.validate(request))
                .thenThrow(new UnauthorizedException("Credenciais inválidas."));

        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(UnauthorizedException.class);

        verify(rateLimiterService, never()).recordSuccess(any());
    }

    @Test
    void refreshRotatesTokenAndIssuesNewAccessToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("hideo@example.com")
                .username("hideo")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        Instant refreshExpiresAt = Instant.now().plusSeconds(3600);
        RefreshTokenService.IssuedToken issuedToken = new RefreshTokenService.IssuedToken("new-refresh-token", refreshExpiresAt);

        when(refreshTokenService.rotate("old-refresh-token"))
                .thenReturn(new RefreshTokenService.RotationResult(user, issuedToken));
        when(jwtService.generateToken(user)).thenReturn("new-access-token");

        AuthService.AuthResult result = authService.refresh("old-refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(result.user()).isEqualTo(user);
    }

    @Test
    void refreshRejectsBlankToken() {
        assertThatThrownBy(() -> authService.refresh(" "))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void logoutRevokesRefreshToken() {
        authService.logout("refresh-token");

        verify(refreshTokenService).revoke("refresh-token");
    }

    @Test
    void logoutIgnoresBlankToken() {
        authService.logout(" ");

        verify(refreshTokenService, never()).revoke(any());
    }

    @Test
    void switchAccountSuccessfullyRotatesAndIssuesNewTokens() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("player@example.com")
                .username("player")
                .status(UserStatus.ACTIVE)
                .build();

        com.hokyozu.kyofuse.auth.dto.request.SwitchAccountRequest request =
                new com.hokyozu.kyofuse.auth.dto.request.SwitchAccountRequest(userId, "raw-switch-token", "device-1");

        when(accountSwitchService.validateAndRotate(userId, "raw-switch-token", "device-1"))
                .thenReturn(new com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchService.SwitchValidationResult(user, "new-rotated-switch-token"));
        when(jwtService.generateToken(user)).thenReturn("new-access-jwt");
        when(refreshTokenService.issue(user)).thenReturn(new RefreshTokenService.IssuedToken("new-refresh-raw", Instant.now().plusSeconds(3600)));

        AuthService.AuthResult result = authService.switchAccount(request, "127.0.0.1", "Mozilla");

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.accessToken()).isEqualTo("new-access-jwt");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-raw");
        assertThat(result.switchToken()).isEqualTo("new-rotated-switch-token");
        verify(rateLimiterService).recordSuccess("switch:ip:127.0.0.1");
    }

    @Test
    void switchAccountRejectsInactiveUser() {
        UUID userId = UUID.randomUUID();
        User inactiveUser = User.builder()
                .id(userId)
                .email("banned@example.com")
                .username("banned")
                .status(UserStatus.BANNED)
                .build();

        com.hokyozu.kyofuse.auth.dto.request.SwitchAccountRequest request =
                new com.hokyozu.kyofuse.auth.dto.request.SwitchAccountRequest(userId, "raw-switch-token", "device-1");

        when(accountSwitchService.validateAndRotate(userId, "raw-switch-token", "device-1"))
                .thenReturn(new com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchService.SwitchValidationResult(inactiveUser, "new-token"));

        assertThatThrownBy(() -> authService.switchAccount(request, "127.0.0.1", "Mozilla"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("não está ativo");
    }

    @Test
    void disconnectAccountDelegatesToAccountSwitchServiceWithValidation() {
        UUID userId = UUID.randomUUID();
        com.hokyozu.kyofuse.auth.dto.request.DisconnectAccountRequest request =
                new com.hokyozu.kyofuse.auth.dto.request.DisconnectAccountRequest(userId, "device-1", "token-abc");

        authService.disconnectAccount(request, userId);

        verify(accountSwitchService).revokeSessionWithValidation(userId, "device-1", "token-abc", userId);
    }

    @Test
    void generateSwitchTokenCreatesSessionForUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("player").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountSwitchService.createOrUpdateSession(user, "device-1")).thenReturn("generated-token");

        String token = authService.generateSwitchToken(userId, "device-1");

        assertThat(token).isEqualTo("generated-token");
    }

    @Test
    void publishLoginSuccessResolvesLocationAndPublishesEvent() {
        User user = User.builder().id(UUID.randomUUID()).username("player").build();
        com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo location =
                com.hokyozu.kyofuse.infrastructure.geolocation.LocationInfo.of(CLIENT_IP, "Curitiba", "Paraná", "Brasil", "BR");

        when(geoLocationService.resolveLocation(CLIENT_IP)).thenReturn(location);

        authService.publishLoginSuccess(user, CLIENT_IP, "Mozilla/5.0");

        ArgumentCaptor<com.hokyozu.kyofuse.auth.event.UserLoginSuccessEvent> captor =
                ArgumentCaptor.forClass(com.hokyozu.kyofuse.auth.event.UserLoginSuccessEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        com.hokyozu.kyofuse.auth.event.UserLoginSuccessEvent event = captor.getValue();
        assertThat(event.user()).isEqualTo(user);
        assertThat(event.clientIp()).isEqualTo(CLIENT_IP);
        assertThat(event.userAgent()).isEqualTo("Mozilla/5.0");
        assertThat(event.location()).isEqualTo(location);
    }
}
