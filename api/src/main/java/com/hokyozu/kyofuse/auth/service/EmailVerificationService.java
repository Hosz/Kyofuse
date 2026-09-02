package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.entity.EmailVerificationToken;
import com.hokyozu.kyofuse.auth.mapper.EmailVerificationTokenMapper;
import com.hokyozu.kyofuse.auth.repository.EmailVerificationTokenRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
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
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailCipherService emailCipherService;
    private final MailService mailService;
    private final RateLimiterService rateLimiterService;
    private final RateLimitPolicies rateLimitPolicies;

    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final String RESEND_VERIFICATION_IP_PREFIX = "resend-verification:ip:";

    @Transactional
    public void createVerificationToken(User user) {
        tokenRepository.deleteAllByUser(user);

        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationTokenMapper.toEntity(user, tokenHash, TOKEN_TTL);
        tokenRepository.save(verificationToken);

        mailService.sendEmailVerificationEmail(user.getEmail(), rawToken);
    }

    public EmailVerificationToken validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Token de verificação inválido ou expirado.");
        }

        String tokenHash = hashToken(rawToken);
        EmailVerificationToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Token de verificação inválido ou expirado."));

        if (token.isExpired() || token.isUsed()) {
            throw new BadRequestException("Token de verificação inválido ou expirado.");
        }

        return token;
    }

    @Transactional
    public User verifyEmail(String rawToken) {
        EmailVerificationToken token = validateToken(rawToken);

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        return user;
    }

    @Transactional
    public void resendVerification(String emailOrUsername, String clientIp) {
        rateLimiterService.checkAndConsume(RESEND_VERIFICATION_IP_PREFIX + clientIp, rateLimitPolicies.register());

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
        if (user.isEmailVerified()) {
            return;
        }

        createVerificationToken(user);
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
