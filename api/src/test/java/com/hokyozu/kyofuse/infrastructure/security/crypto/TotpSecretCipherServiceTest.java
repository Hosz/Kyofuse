package com.hokyozu.kyofuse.infrastructure.security.crypto;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TotpSecretCipherServiceTest {

    private final TotpSecretCipherService totpSecretCipherService = new TotpSecretCipherService(randomBase64Key());

    @Test
    void encryptThenDecryptRoundTripsToTheOriginalSecret() {
        String encrypted = totpSecretCipherService.encrypt("JBSWY3DPEHPK3PXP");

        assertThat(encrypted).isNotEqualTo("JBSWY3DPEHPK3PXP");
        assertThat(totpSecretCipherService.decrypt(encrypted)).isEqualTo("JBSWY3DPEHPK3PXP");
    }

    @Test
    void encryptingTheSameValueTwiceProducesDifferentCiphertext() {
        String first = totpSecretCipherService.encrypt("JBSWY3DPEHPK3PXP");
        String second = totpSecretCipherService.encrypt("JBSWY3DPEHPK3PXP");

        assertThat(first).isNotEqualTo(second);
        assertThat(totpSecretCipherService.decrypt(first)).isEqualTo(totpSecretCipherService.decrypt(second));
    }

    @Test
    void decryptingWithADifferentKeyFails() {
        String encrypted = totpSecretCipherService.encrypt("JBSWY3DPEHPK3PXP");
        TotpSecretCipherService otherInstance = new TotpSecretCipherService(randomBase64Key());

        assertThatThrownBy(() -> otherInstance.decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }

    private static String randomBase64Key() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
