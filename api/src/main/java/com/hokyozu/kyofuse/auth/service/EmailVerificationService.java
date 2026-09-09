package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.entity.EmailVerificationToken;
import com.hokyozu.kyofuse.auth.mapper.EmailVerificationTokenMapper;
import com.hokyozu.kyofuse.auth.repository.EmailVerificationTokenRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimitPolicies;
import com.hokyozu.kyofuse.infrastructure.security.ratelimit.RateLimiterService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
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
import java.util.UUID;

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
        verificationToken.setTokenType("REGISTRATION");
        tokenRepository.save(verificationToken);

        mailService.sendEmailVerificationEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public void createLinkVerificationToken(User user, String targetEmail, String targetEmailIndex) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = EmailVerificationTokenMapper.toEntity(user, tokenHash, TOKEN_TTL);
        verificationToken.setTokenType("LINK_EMAIL");
        verificationToken.setPendingEmail(targetEmail);
        verificationToken.setPendingEmailIndex(targetEmailIndex);
        tokenRepository.save(verificationToken);

        mailService.sendEmailLinkVerificationEmail(targetEmail, rawToken);
    }

    @Transactional(readOnly = true)
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

        if ("LINK_EMAIL".equalsIgnoreCase(token.getTokenType())) {
            if (userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot(token.getPendingEmailIndex(), token.getUser().getId())) {
                throw new ConflictException("Este e-mail já foi verificado e pertence a outra conta.");
            }
        } else {
            if (userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot(token.getUser().getEmailIndex(), token.getUser().getId())) {
                throw new ConflictException("Este e-mail já foi verificado e pertence a outra conta.");
            }
        }

        return token;
    }

    @Transactional
    public User verifyEmail(String rawToken) {
        EmailVerificationToken token = validateToken(rawToken);
        User user = token.getUser();

        if ("LINK_EMAIL".equalsIgnoreCase(token.getTokenType())) {
            String pendingEmailIndex = token.getPendingEmailIndex();
            if (userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot(pendingEmailIndex, user.getId())) {
                throw new ConflictException("Este e-mail já foi verificado e pertence a outra conta.");
            }

            cleanUpOtherUnverifiedUsers(pendingEmailIndex, user.getId());

            user.setEmail(token.getPendingEmail());
            user.setEmailIndex(pendingEmailIndex);
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);

            token.setUsedAt(Instant.now());
            tokenRepository.save(token);

            tokenRepository.deleteAllByPendingEmailIndex(pendingEmailIndex);
            return user;
        }

        // REGISTRATION
        String emailIndex = user.getEmailIndex();
        if (userRepository.existsByEmailIndexAndEmailVerifiedTrueAndIdNot(emailIndex, user.getId())) {
            throw new ConflictException("Este e-mail já foi verificado e pertence a outra conta.");
        }
        if (userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrueAndIdNot(user.getUsername(), user.getId())) {
            throw new ConflictException("Este nome de usuário já está em uso.");
        }

        cleanUpOtherUnverifiedUsers(emailIndex, user.getId());

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        tokenRepository.deleteAllByPendingEmailIndex(emailIndex);
        return user;
    }

    private void cleanUpOtherUnverifiedUsers(String emailIndex, UUID keepUserId) {
        if (emailIndex == null || emailIndex.isBlank()) {
            return;
        }
        var unverified = userRepository.findAllByEmailIndexAndEmailVerifiedFalse(emailIndex);
        for (User other : unverified) {
            if (keepUserId == null || !other.getId().equals(keepUserId)) {
                tokenRepository.deleteAllByUser(other);
                userRepository.delete(other);
            }
        }
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
