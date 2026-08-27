package com.hokyozu.kyofuse.auth.service;

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
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.infrastructure.security.totp.MfaTokenService;
import com.hokyozu.kyofuse.infrastructure.security.totp.RecoveryCodeService;
import com.hokyozu.kyofuse.infrastructure.security.totp.TotpService;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import com.hokyozu.kyofuse.relationships.privacy.service.UserPrivacySettingsService;
import com.hokyozu.kyofuse.shared.exception.RefreshTokenAbsentException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final GamerProfileService gamerProfileService;
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
    public AuthResult register(RegisterRequest request, String clientIp) {

       rateLimiterService.checkAndConsume(REGISTER_IP_KEY_PREFIX + clientIp, rateLimitPolicies.register());

       String emailIndex = emailCipherService.blindIndex(request.email());
       emailAndUsernameAvailabilityValidator.validate(emailIndex, request.username());
       String passwordHash = passwordEncoder.encode(request.password());
       User user = AuthMapper.toEntity(request, passwordHash, emailIndex);
       userRepository.save(user);
       gamerProfileService.createGamerProfileMin(user);
       userPrivacySettingsService.createDefault(user);

       return issueTokens(user);
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
