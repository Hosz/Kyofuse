package com.hokyozu.kyofuse.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.mapper.AuthMapper;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.validator.EmailAndUsernameAvailabilityValidator;
import com.hokyozu.kyofuse.auth.validator.LoginFinderValidator;
import com.hokyozu.kyofuse.auth.validator.LoginValidator;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenService;
import com.hokyozu.kyofuse.infrastructure.security.oauth.GoogleTokenVerifierService;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamPlayerSummary;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamService;
import com.hokyozu.kyofuse.infrastructure.security.totp.MfaTokenService;
import com.hokyozu.kyofuse.infrastructure.security.totp.RecoveryCodeService;
import com.hokyozu.kyofuse.infrastructure.security.totp.TotpService;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import com.hokyozu.kyofuse.profiles.service.SteamProfileSyncService;
import com.hokyozu.kyofuse.relationships.privacy.service.UserPrivacySettingsService;
import com.hokyozu.kyofuse.shared.exception.RefreshTokenAbsentException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final GamerProfileService gamerProfileService;
    private final SteamProfileSyncService steamProfileSyncService;
    private final UserPrivacySettingsService userPrivacySettingsService;

    private final EmailAndUsernameAvailabilityValidator emailAndUsernameAvailabilityValidator;
    private final LoginFinderValidator loginFinderValidator;
    private final LoginValidator loginValidator;

    private final RateLimiterService rateLimiterService;
    private final RateLimitPolicies rateLimitPolicies;

    private final EmailCipherService emailCipherService;

    private final MfaTokenService mfaTokenService;
    private final TotpService totpService;
    private final RecoveryCodeService recoveryCodeService;

    private final EmailVerificationService emailVerificationService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final SteamService steamService;

    private static final String LOGIN_IP_KEY_PREFIX = "login:ip:";
    private static final String LOGIN_USER_KEY_PREFIX = "login:user:";
    private static final String REGISTER_IP_KEY_PREFIX = "register:ip:";
    private static final String MFA_USER_KEY_PREFIX = "mfa:user:";

    public record AuthResult(User user, String accessToken, String refreshToken, Instant refreshTokenExpiresAt) {}

    public sealed interface LoginOutcome {
        record Authenticated(AuthResult result) implements LoginOutcome {}
        record MfaRequired(String mfaToken) implements LoginOutcome {}
    }

    @Transactional
    public User register(RegisterRequest request, String clientIp) {
        rateLimiterService.checkAndConsume(REGISTER_IP_KEY_PREFIX + clientIp, rateLimitPolicies.register());

        String emailIndex = emailCipherService.blindIndex(request.email());
        emailAndUsernameAvailabilityValidator.validate(emailIndex, request.username());
        String passwordHash = passwordEncoder.encode(request.password());
        User user = AuthMapper.toEntity(request, passwordHash, emailIndex);
        userRepository.save(user);
        gamerProfileService.createGamerProfileMin(user);
        userPrivacySettingsService.createDefault(user);

        emailVerificationService.createVerificationToken(user);

        return user;
    }

    public LoginOutcome login(LoginRequest request, String clientIp) {
        String ipKey = LOGIN_IP_KEY_PREFIX + clientIp;
        String userKey = LOGIN_USER_KEY_PREFIX + request.login().trim().toLowerCase();

        rateLimiterService.checkAndConsume(ipKey, rateLimitPolicies.login());
        rateLimiterService.checkAndConsume(userKey, rateLimitPolicies.login());

        User user = loginFinderValidator.validate(request);
        loginValidator.validate(user, request);

        rateLimiterService.recordSuccess(ipKey);
        rateLimiterService.recordSuccess(userKey);

        if (user.isTotpEnabled()) {
            return new LoginOutcome.MfaRequired(mfaTokenService.generate(user));
        }

        return new LoginOutcome.Authenticated(issueTokens(user));
    }

    @Transactional
    public LoginOutcome loginWithGoogle(String idTokenString, String clientIp) {
        String ipKey = LOGIN_IP_KEY_PREFIX + clientIp;
        rateLimiterService.checkAndConsume(ipKey, rateLimitPolicies.login());

        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(idTokenString);
        String email = payload.getEmail();
        String emailIndex = emailCipherService.blindIndex(email);

        Optional<User> existingUserOpt = userRepository.findByEmailIndex(emailIndex);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();

            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                user.setEmailVerifiedAt(Instant.now());
                user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setUpdatedAt(Instant.now());
                userRepository.save(user);
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("Usuário não está ativo.");
            }
        } else {
            user = createGoogleUser(payload, emailIndex);
        }

        rateLimiterService.recordSuccess(ipKey);

        if (user.isTotpEnabled()) {
            return new LoginOutcome.MfaRequired(mfaTokenService.generate(user));
        }

        return new LoginOutcome.Authenticated(issueTokens(user));
    }

    public LoginOutcome loginWithSteam(Map<String, String> openIdParams, String clientIp) {
        String ipKey = LOGIN_IP_KEY_PREFIX + clientIp;
        rateLimiterService.checkAndConsume(ipKey, rateLimitPolicies.login());

        String steamId = steamService.validateOpenIdAndGetSteamId(openIdParams);

        Optional<User> existingUserOpt = userRepository.findBySteamId(steamId);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException("Usuário não está ativo.");
            }

            Optional<SteamPlayerSummary> summaryOpt = steamService.getPlayerSummary(steamId);
            summaryOpt.ifPresent(summary -> steamProfileSyncService.syncIfMissing(user, summary));
        } else {
            Optional<SteamPlayerSummary> summaryOpt = steamService.getPlayerSummary(steamId);
            user = createSteamUser(steamId, summaryOpt.orElse(null));
        }

        rateLimiterService.recordSuccess(ipKey);

        if (user.isTotpEnabled()) {
            return new LoginOutcome.MfaRequired(mfaTokenService.generate(user));
        }

        return new LoginOutcome.Authenticated(issueTokens(user));
    }

    private User createSteamUser(String steamId, SteamPlayerSummary summary) {
        String personaName = (summary != null && summary.personaName() != null && !summary.personaName().isBlank())
                ? summary.personaName()
                : "steam_" + steamId.substring(Math.max(0, steamId.length() - 6));

        String syntheticEmail = "steam_" + steamId + "@steam.kyofuse.local";
        String emailIndex = emailCipherService.blindIndex(syntheticEmail);

        String uniqueUsername = generateUniqueUsername(personaName);
        String passwordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = AuthMapper.toSteamEntity(steamId, personaName, syntheticEmail, emailIndex, uniqueUsername, passwordHash);

        userRepository.save(newUser);

        String avatarUrl = summary != null ? summary.avatarFull() : null;
        String country = summary != null ? summary.locCountryCode() : null;

        gamerProfileService.createGamerProfile(newUser, personaName, avatarUrl, country);
        userPrivacySettingsService.createDefault(newUser);

        return newUser;
    }

    private User createGoogleUser(GoogleIdToken.Payload payload, String emailIndex) {
        String givenName = (String) payload.get("given_name");
        String familyName = (String) payload.get("family_name");
        String name = (String) payload.get("name");
        String email = payload.getEmail();

        if (givenName == null || givenName.isBlank()) {
            givenName = (name != null && !name.isBlank()) ? name : "Usuário";
        }
        if (familyName == null || familyName.isBlank()) {
            familyName = "Google";
        }

        String baseUsername = email.split("@")[0];
        String uniqueUsername = generateUniqueUsername(baseUsername);
        String passwordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        User newUser = AuthMapper.toGoogleEntity(givenName, familyName, email, emailIndex, uniqueUsername, passwordHash);

        userRepository.save(newUser);
        gamerProfileService.createGamerProfileMin(newUser);
        userPrivacySettingsService.createDefault(newUser);

        return newUser;
    }

    private String generateUniqueUsername(String baseUsername) {
        String cleaned = baseUsername.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (cleaned.isBlank()) {
            cleaned = "user";
        }
        if (cleaned.length() > 25) {
            cleaned = cleaned.substring(0, 25);
        }

        if (!userRepository.existsByUsernameIgnoreCase(cleaned)) {
            return cleaned;
        }

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 100; i++) {
            String candidate = cleaned + (random.nextInt(9000) + 1000);
            if (!userRepository.existsByUsernameIgnoreCase(candidate)) {
                return candidate;
            }
        }

        return cleaned + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public AuthResult verifyEmail(String token) {
        User user = emailVerificationService.verifyEmail(token);
        return issueTokens(user);
    }

    public AuthResult verifyMfa(String mfaToken, String code) {
        if (mfaToken == null || mfaToken.isBlank()) {
            throw new UnauthorizedException("Token de verificação ausente.");
        }

        UUID userId = mfaTokenService.resolveUserId(mfaToken);
        String rateLimitKey = MFA_USER_KEY_PREFIX + userId;
        rateLimiterService.checkAndConsume(rateLimitKey, rateLimitPolicies.mfa());

        User user = userRepository.findById(userId)
                .filter(User::isTotpEnabled)
                .orElseThrow(() -> new UnauthorizedException("Token de verificação inválido."));

        boolean valid = totpService.verifyCode(user.getTotpSecret(), code)
                || recoveryCodeService.consume(user, code);

        if (!valid) {
            throw new UnauthorizedException("Código de verificação inválido.");
        }

        rateLimiterService.recordSuccess(rateLimitKey);

        return issueTokens(user);
    }

    public AuthResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new RefreshTokenAbsentException();
        }

        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(rawRefreshToken);
        String accessToken = jwtService.generateToken(rotation.user());

        return new AuthResult(
                rotation.user(),
                accessToken,
                rotation.issuedToken().rawToken(),
                rotation.issuedToken().expiresAt()
        );
    }

    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revoke(rawRefreshToken);
        }
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshTokenService.IssuedToken refreshToken = refreshTokenService.issue(user);

        return new AuthResult(user, accessToken, refreshToken.rawToken(), refreshToken.expiresAt());
    }
}
