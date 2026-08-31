package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.entity.PasswordResetToken;
import com.hokyozu.kyofuse.auth.mapper.PasswordResetTokenMapper;
import com.hokyozu.kyofuse.auth.repository.PasswordResetTokenRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchSessionRepository;
import com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountSwitchSessionRepository accountSwitchSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailCipherService emailCipherService;
    private final MailService mailService;
    private final RateLimiterService rateLimiterService;
    private final RateLimitPolicies rateLimitPolicies;

    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final String FORGOT_PASSWORD_IP_PREFIX = "forgot-password:ip:";

    @Transactional
    public void  requestPasswordReset(String emailOrUsername, String clientIp) {
        rateLimiterService.checkAndConsume(FORGOT_PASSWORD_IP_PREFIX + clientIp, rateLimitPolicies.login());

        Optional<User> userOpt;
        if (emailOrUsername.contains("@")) {
            String emailIndex = emailCipherService.blindIndex(emailOrUsername);
            userOpt = userRepository.findByEmailIndex(emailIndex);
        } else {
            userOpt = userRepository.findByUsernameIgnoreCase(emailOrUsername);
        }

        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        tokenRepository.deleteAllByUser(user);

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = PasswordResetTokenMapper.toEntity(user, tokenHash, TOKEN_TTL);
        tokenRepository.save(resetToken);

        mailService.sendPasswordResetEmail(user.getEmail(), rawToken);
    }

    public void validateToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Token de recuperação inválido ou expirado."));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new BadRequestException("Token de recuperação inválido ou expirado.");
        }
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Token de recuperação inválido ou expirado."));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new BadRequestException("Token de recuperação inválido ou expirado.");
        }

        User user = resetToken.getUser();
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        tokenRepository.save(resetToken);

        refreshTokenRepository.deleteAllByUser(user);
        accountSwitchSessionRepository.deleteAllByUserId(user.getId());
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Erro ao calcular hash do token.", e);
        }
    }
}
