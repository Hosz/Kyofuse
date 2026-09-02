package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MfaTokenServiceTest {

    private static final String SECRET = "mfa-test-secret-key-with-at-least-32-bytes!!";

    private final MfaTokenService mfaTokenService = new MfaTokenService(SECRET, 5);

    @Test
    void generateThenResolveUserIdRoundTrips() {
        User user = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();

        String token = mfaTokenService.generate(user);

        assertThat(mfaTokenService.resolveUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void resolveUserIdRejectsGarbageToken() {
        assertThatThrownBy(() -> mfaTokenService.resolveUserId("not-a-jwt"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void resolveUserIdRejectsTokenSignedWithADifferentSecret() {
        MfaTokenService otherInstance = new MfaTokenService("a-completely-different-secret-key-32-bytes!", 5);
        User user = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();

        String token = otherInstance.generate(user);

        assertThatThrownBy(() -> mfaTokenService.resolveUserId(token))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void resolveUserIdRejectsTokenWithoutMfaPendingPurpose() {
        SecretKey secretKey = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder rawEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("kyofuse-api")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .subject(UUID.randomUUID().toString())
                .build();

        String tokenWithoutPurpose = rawEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        assertThatThrownBy(() -> mfaTokenService.resolveUserId(tokenWithoutPurpose))
                .isInstanceOf(UnauthorizedException.class);
    }
}
