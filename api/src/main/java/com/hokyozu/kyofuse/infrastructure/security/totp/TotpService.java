package com.hokyozu.kyofuse.infrastructure.security.totp;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

/**
 * Wrapper fino em cima da lib dev.samstevens.totp. O secret nunca é comparado por
 * igualdade nem guardado em texto puro fora daqui — quem persiste (via
 * {@code User.totpSecret}) cuida da cifragem através do
 * {@link com.hokyozu.kyofuse.infrastructure.security.crypto.EncryptedTotpSecretConverter}.
 */
@Service
public class TotpService {

    private static final String ISSUER = "Kyofuse";

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public String buildOtpAuthUri(String accountLabel, String secret) {
        QrData data = new QrData.Builder()
                .label(accountLabel)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        return data.getUri();
    }

    public boolean verifyCode(String secret, String code) {
        return secret != null && code != null && !code.isBlank() && codeVerifier.isValidCode(secret, code);
    }
}
