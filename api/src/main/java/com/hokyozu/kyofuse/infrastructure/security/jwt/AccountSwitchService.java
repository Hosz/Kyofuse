package com.hokyozu.kyofuse.infrastructure.security.jwt;

import com.hokyozu.kyofuse.infrastructure.entity.AccountSwitchSession;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountSwitchService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long DEFAULT_EXPIRATION_DAYS = 60;

    private final AccountSwitchSessionRepository sessionRepository;

    @Value("${security.account-switch.expiration-days:60}")
    private long switchTokenExpirationDays = DEFAULT_EXPIRATION_DAYS;

    public record SwitchValidationResult(User user, String newSwitchToken) {}

    @Transactional
    public String createOrUpdateSession(User user, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        String rawToken = generateRawToken();
        String tokenHash = hash(rawToken);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(switchTokenExpirationDays, ChronoUnit.DAYS);

        Optional<AccountSwitchSession> existing = sessionRepository.findByUserIdAndDeviceId(user.getId(), deviceId);

        AccountSwitchSession session;
        if (existing.isPresent()) {
            session = existing.get();
            session.setSwitchTokenHash(tokenHash);
            session.setExpiresAt(expiresAt);
            session.setUpdatedAt(now);
        } else {
            session = AccountSwitchSession.builder()
                    .user(user)
                    .deviceId(deviceId.trim())
                    .switchTokenHash(tokenHash)
                    .expiresAt(expiresAt)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }

        sessionRepository.save(session);
        return rawToken;
    }

    @Transactional
    public SwitchValidationResult validateAndRotate(UUID targetUserId, String rawSwitchToken, String deviceId) {
        if (rawSwitchToken == null || rawSwitchToken.isBlank() || deviceId == null || deviceId.isBlank()) {
            throw new UnauthorizedException("Credenciais de alternância de conta inválidas.");
        }

        String tokenHash = hash(rawSwitchToken);
        AccountSwitchSession session = sessionRepository.findBySwitchTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Sessão de troca de conta não encontrada ou expirada."));

        if (!session.getUser().getId().equals(targetUserId) || !session.getDeviceId().equals(deviceId.trim())) {
            sessionRepository.delete(session);
            throw new UnauthorizedException("Sessão não corresponde ao dispositivo ou usuário informado.");
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {
            sessionRepository.delete(session);
            throw new UnauthorizedException("Sessão de troca de conta expirada. Faça login novamente.");
        }

        String newRawToken = generateRawToken();
        Instant now = Instant.now();
        session.setSwitchTokenHash(hash(newRawToken));
        session.setExpiresAt(now.plus(switchTokenExpirationDays, ChronoUnit.DAYS));
        session.setUpdatedAt(now);
        sessionRepository.save(session);

        return new SwitchValidationResult(session.getUser(), newRawToken);
    }

    @Transactional
    public void revokeSessionWithValidation(UUID targetUserId, String deviceId, String rawSwitchToken, UUID currentAuthenticatedUserId) {
        if (targetUserId == null || deviceId == null || deviceId.isBlank()) {
            throw new UnauthorizedException("Parâmetros de desconexão inválidos.");
        }

        // Cenário 1: O usuário autenticado está desconectando sua própria conta deste dispositivo
        if (currentAuthenticatedUserId != null && currentAuthenticatedUserId.equals(targetUserId)) {
            revokeSession(targetUserId, deviceId);
            return;
        }

        // Cenário 2: O chamador fornece o switchToken correspondente à conta e dispositivo
        if (rawSwitchToken != null && !rawSwitchToken.isBlank()) {
            String tokenHash = hash(rawSwitchToken);
            Optional<AccountSwitchSession> sessionOpt = sessionRepository.findBySwitchTokenHash(tokenHash);
            if (sessionOpt.isPresent()) {
                AccountSwitchSession session = sessionOpt.get();
                if (session.getUser().getId().equals(targetUserId) && session.getDeviceId().equals(deviceId.trim())) {
                    sessionRepository.delete(session);
                    return;
                }
            }
        }

        throw new UnauthorizedException("Não autorizado a desconectar esta conta.");
    }

    @Transactional
    public void revokeSession(UUID userId, String deviceId) {
        if (userId != null && deviceId != null && !deviceId.isBlank()) {
            sessionRepository.deleteByUserIdAndDeviceId(userId, deviceId.trim());
        }
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        if (userId != null) {
            sessionRepository.deleteAllByUserId(userId);
        }
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
