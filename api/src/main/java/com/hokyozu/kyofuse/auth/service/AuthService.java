package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.mapper.AuthMapper;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.validator.EmailAndUsernameAvailabilityValidator;
import com.hokyozu.kyofuse.auth.validator.LoginFinderValidator;
import com.hokyozu.kyofuse.auth.validator.LoginValidator;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenService;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import com.hokyozu.kyofuse.relationships.privacy.service.UserPrivacySettingsService;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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

    private static final String LOGIN_IP_KEY_PREFIX = "login:ip:";
    private static final String LOGIN_USER_KEY_PREFIX = "login:user:";
    private static final String REGISTER_IP_KEY_PREFIX = "register:ip:";

    public record AuthResult(User user, String accessToken, String refreshToken, Instant refreshTokenExpiresAt) {}

    @Transactional
    public AuthResult register(RegisterRequest request, String clientIp) {

       rateLimiterService.checkAndConsume(REGISTER_IP_KEY_PREFIX + clientIp, rateLimitPolicies.register());

       emailAndUsernameAvailabilityValidator.validate(request.email(), request.username());
       String passwordHash = passwordEncoder.encode(request.password());
       User user = AuthMapper.toEntity(request, passwordHash);
       userRepository.save(user);
       gamerProfileService.createGamerProfileMin(user);
       userPrivacySettingsService.createDefault(user);

       return issueTokens(user);
    }

    public AuthResult login(LoginRequest request, String clientIp) {

        String ipKey = LOGIN_IP_KEY_PREFIX + clientIp;
        String userKey = LOGIN_USER_KEY_PREFIX + request.login().trim().toLowerCase();

        rateLimiterService.checkAndConsume(ipKey, rateLimitPolicies.login());
        rateLimiterService.checkAndConsume(userKey, rateLimitPolicies.login());

        User user = loginFinderValidator.validate(request);
        loginValidator.validate(user, request);

        rateLimiterService.recordSuccess(ipKey);
        rateLimiterService.recordSuccess(userKey);

        return issueTokens(user);
    }

    public AuthResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token ausente.");
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
