package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.validator.EmailAndUsernameAvailabilityValidator;
import com.hokyozu.kyofuse.auth.validator.LoginFinderValidator;
import com.hokyozu.kyofuse.auth.validator.LoginValidator;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private AuthService authService;

    private static final String CLIENT_IP = "203.0.113.10";

    @Test
    void registerCreatesUserProfileAndTokens() {
        RegisterRequest request = new RegisterRequest(
                " Hideo ",
                " Kojima ",
                " hideo@example.com ",
                " hideo ",
                "password123"
        );
        UUID userId = UUID.randomUUID();
        Instant refreshExpiresAt = Instant.now().plusSeconds(3600);

        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(emailCipherService.blindIndex(" hideo@example.com ")).thenReturn("email-index-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(refreshTokenService.issue(any(User.class)))
                .thenReturn(new RefreshTokenService.IssuedToken("refresh-token", refreshExpiresAt));

        AuthService.AuthResult result = authService.register(request, CLIENT_IP);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(rateLimiterService).checkAndConsume(eq("register:ip:" + CLIENT_IP), any());
        verify(emailAndUsernameAvailabilityValidator).validate("email-index-hash", " hideo ");
        verify(userRepository).save(userCaptor.capture());
        verify(gamerProfileService).createGamerProfileMin(userCaptor.getValue());
        verify(userPrivacySettingsService).createDefault(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstName()).isEqualTo("Hideo");
        assertThat(savedUser.getLastName()).isEqualTo("Kojima");
        assertThat(savedUser.getEmail()).isEqualTo("hideo@example.com");
        assertThat(savedUser.getEmailIndex()).isEqualTo("email-index-hash");
        assertThat(savedUser.getUsername()).isEqualTo("hideo");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.refreshTokenExpiresAt()).isEqualTo(refreshExpiresAt);
        assertThat(result.user().getId()).isEqualTo(userId);
        assertThat(result.user().getEmail()).isEqualTo("hideo@example.com");
        assertThat(result.user().getUsername()).isEqualTo("hideo");
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
}
