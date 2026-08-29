package com.hokyozu.kyofuse.infrastructure.security.jwt;

import com.hokyozu.kyofuse.auth.mapper.RefreshTokenMapper;
import com.hokyozu.kyofuse.infrastructure.entity.RefreshToken;
import com.hokyozu.kyofuse.shared.exception.RefreshTokenExpiredException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Refresh tokens são opacos e stateful: só o hash SHA-256 é persistido, e cada uso
 * rotaciona o token (revoga o atual, emite um novo na mesma "família"). Se um token já
 * revogado for reapresentado, é sinal de token vazado/reuso, e a família inteira é
 * revogada para forçar novo login em todas as sessões daquele usuário.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    public record IssuedToken(String rawToken, Instant expiresAt) {}

    public record RotationResult(User user, IssuedToken issuedToken) {}

    @Transactional
    public IssuedToken issue(User user) {
        return persist(user, UUID.randomUUID());
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RefreshTokenExpiredException("Refresh token inválido."));

        if (current.isRevoked()) {
            refreshTokenRepository.revokeAllByFamilyId(current.getFamilyId());
            throw new RefreshTokenExpiredException("Refresh token já utilizado.");
        }

        if (current.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredException("Refresh token expirado.");
        }

        User user = current.getUser();
        IssuedToken next = persist(user, current.getFamilyId());

        current.setRevoked(true);
        refreshTokenRepository.save(current);

        return new RotationResult(user, next);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private IssuedToken persist(User user, UUID familyId) {
        String rawToken = generateRawToken();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = RefreshTokenMapper
                .toEntity(user, hash(rawToken), familyId, expiresAt);

        refreshTokenRepository.save(refreshToken);

        return new IssuedToken(rawToken, expiresAt);
    }

    private static String generateRawToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
