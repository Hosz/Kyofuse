package com.hokyozu.kyofuse.infrastructure.security.reactivation;

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

@Service
public class AccountReactivationTokenService {

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String REACTIVATION_PENDING_PURPOSE = "reactivation_pending";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long expirationMinutes;

    public AccountReactivationTokenService(
            @Value("${security.jwt.reactivation-secret}") String secret,
            @Value("${security.jwt.reactivation-token-expiration-minutes:15}") long expirationMinutes
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
                .claim(PURPOSE_CLAIM, REACTIVATION_PENDING_PURPOSE)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public UUID resolveUserId(String reactivationToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(reactivationToken);
        } catch (JwtException e) {
            throw new UnauthorizedException("Token de reativação inválido ou expirado.");
        }

        if (!REACTIVATION_PENDING_PURPOSE.equals(jwt.getClaimAsString(PURPOSE_CLAIM))) {
            throw new UnauthorizedException("Token de reativação inválido.");
        }

        return UUID.fromString(jwt.getSubject());
    }
}
