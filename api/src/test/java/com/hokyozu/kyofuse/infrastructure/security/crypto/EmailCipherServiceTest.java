package com.hokyozu.kyofuse.infrastructure.security.crypto;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailCipherServiceTest {

    private final EmailCipherService emailCipherService = new EmailCipherService(randomBase64Key(), randomBase64Key());

    @Test
    void encryptThenDecryptRoundTripsToTheOriginalPlaintext() {
        String encrypted = emailCipherService.encrypt("hideo@example.com");

        assertThat(encrypted).isNotEqualTo("hideo@example.com");
        assertThat(emailCipherService.decrypt(encrypted)).isEqualTo("hideo@example.com");
    }

    @Test
    void encryptingTheSameValueTwiceProducesDifferentCiphertext() {
        String first = emailCipherService.encrypt("hideo@example.com");
        String second = emailCipherService.encrypt("hideo@example.com");

        assertThat(first).isNotEqualTo(second);
        assertThat(emailCipherService.decrypt(first)).isEqualTo(emailCipherService.decrypt(second));
    }

    @Test
    void decryptingWithADifferentKeyFails() {
        String encrypted = emailCipherService.encrypt("hideo@example.com");
        EmailCipherService otherInstance = new EmailCipherService(randomBase64Key(), randomBase64Key());

        assertThatThrownBy(() -> otherInstance.decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blindIndexIsDeterministicForTheSameNormalizedEmail() {
        String indexA = emailCipherService.blindIndex("Hideo@Example.com");
        String indexB = emailCipherService.blindIndex("  hideo@example.com  ");

        assertThat(indexA).isEqualTo(indexB);
        assertThat(indexA).hasSize(64);
    }

    @Test
    void blindIndexDoesNotMatchAcrossDifferentEmails() {
        String indexA = emailCipherService.blindIndex("hideo@example.com");
        String indexB = emailCipherService.blindIndex("solid@example.com");

        assertThat(indexA).isNotEqualTo(indexB);
    }

    @Test
    void blindIndexIsNotReversibleIntoTheEmail() {
        String index = emailCipherService.blindIndex("hideo@example.com");

        assertThat(index).doesNotContain("hideo");
    }

    private static String randomBase64Key() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
