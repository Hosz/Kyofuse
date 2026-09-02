package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Token curto emitido entre "senha validada" e "código TOTP confirmado". Usa um
 * JwtEncoder/JwtDecoder próprios — com um secret separado do access token — de
 * propósito: se usasse o mesmo secret do {@code JwtService}, um mfaToken vazado
 * poderia ser reapresentado como cookie de access token e o resource server
 * (SecurityConfig) o aceitaria como sessão autenticada, pulando o 2FA por completo.
 * Sendo assinado com uma chave diferente, o JwtDecoder do resource server rejeita
 * esse token de cara.
 */
@Service
@RequiredArgsConstructor
public class MfaTokenService {

    private final MfaSessionRepository mfaSessionRepository;

    public String generate(User user) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        MfaSession session = MfaSession.builder()
                .mfaToken(token)
                .userId(user.getId())
                .remainingAttempts(3)
                .createdAt(Instant.now())
                .build();

        mfaSessionRepository.save(session);
        return token;
    }

    public UUID resolveAndValidateUserId(String mfaToken) {
        MfaSession session = mfaSessionRepository.findById(mfaToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid mfa token"));

        if (session.getRemainingAttempts() <= 0) {
            mfaSessionRepository.deleteById(mfaToken);
            throw new UnauthorizedException("Limite de tentativas de 2FA excedido. Faça login novamente.");
        }

        return session.getUserId();
    }

    public UUID resolveUserId(String mfaToken) {
        return resolveAndValidateUserId(mfaToken);
    }

    public void recordFailedAttempt(String mfaToken) {
        mfaSessionRepository.findById(mfaToken).ifPresent(session -> {
            session.setRemainingAttempts(session.getRemainingAttempts() - 1);
            if (session.getRemainingAttempts() <= 0) {
                mfaSessionRepository.deleteById(mfaToken);
            } else {
                mfaSessionRepository.save(session);
            }
        });
    }

    public void consume(String mfaToken) {
        mfaSessionRepository.deleteById(mfaToken);
    }
}
