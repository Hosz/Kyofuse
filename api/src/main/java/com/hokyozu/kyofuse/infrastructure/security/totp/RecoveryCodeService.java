package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.infrastructure.entity.RecoveryCode;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Recovery codes são de uso único, guardados como hash (mesmo esquema de
 * {@code RefreshTokenService}: só o SHA-256 do código é persistido, o valor em texto
 * puro é devolvido uma única vez na confirmação do 2FA).
 */
@Service
@RequiredArgsConstructor
public class RecoveryCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_COUNT = 8;

    private final RecoveryCodeRepository recoveryCodeRepository;

    @Transactional
    public List<String> regenerate(User user) {
        recoveryCodeRepository.deleteAllByUser(user);

        Instant now = Instant.now();
        List<String> rawCodes = new ArrayList<>(CODE_COUNT);
        List<RecoveryCode> entities = new ArrayList<>(CODE_COUNT);

        for (int i = 0; i < CODE_COUNT; i++) {
            String rawCode = generateRawCode();
            rawCodes.add(rawCode);
            entities.add(RecoveryCode.builder()
                    .user(user)
                    .codeHash(hash(rawCode))
                    .createdAt(now)
                    .build());
        }

        recoveryCodeRepository.saveAll(entities);

        return rawCodes;
    }

    @Transactional
    public boolean consume(User user, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return false;
        }

        return recoveryCodeRepository.findByUserAndCodeHashAndUsedAtIsNull(user, hash(rawCode.trim()))
                .map(code -> {
                    code.setUsedAt(Instant.now());
                    recoveryCodeRepository.save(code);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void deleteAll(User user) {
        recoveryCodeRepository.deleteAllByUser(user);
    }

    private static String generateRawCode() {
        byte[] randomBytes = new byte[5];
        SECURE_RANDOM.nextBytes(randomBytes);
        String hex = HexFormat.of().withUpperCase().formatHex(randomBytes);
        return hex.substring(0, 5) + "-" + hex.substring(5);
    }

    private static String hash(String rawCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawCode.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
