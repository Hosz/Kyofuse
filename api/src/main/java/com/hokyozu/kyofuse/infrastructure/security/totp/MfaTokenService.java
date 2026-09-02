package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
public class MfaTokenService {

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String MFA_PENDING_PURPOSE = "mfa_pending";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long expirationMinutes;

    public MfaTokenService(
            @Value("${security.jwt.mfa-secret}") String secret,
            @Value("${security.jwt.mfa-token-expiration-minutes}") long expirationMinutes
    ) {
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(User user) {
        Instant now = Instant.now();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("kyofuse-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationMinutes * 60))
                .subject(user.getId().toString())
                .claim(PURPOSE_CLAIM, MFA_PENDING_PURPOSE)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public UUID resolveUserId(String mfaToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(mfaToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("Token de verificação inválido ou expirado.");
        }

        if (!MFA_PENDING_PURPOSE.equals(jwt.getClaimAsString(PURPOSE_CLAIM))) {
            throw new UnauthorizedException("Token de verificação inválido.");
        }

        return UUID.fromString(jwt.getSubject());
    }
}
