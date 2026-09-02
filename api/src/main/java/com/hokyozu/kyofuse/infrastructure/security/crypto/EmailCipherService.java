package com.hokyozu.kyofuse.infrastructure.security.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Cifra o email pra guardar no banco (AES-GCM, IV aleatório a cada chamada — duas
 * cifragens do mesmo email nunca ficam iguais) e calcula o "blind index" usado pra
 * busca/unicidade (HMAC-SHA256 do email normalizado, determinístico: o mesmo email
 * sempre gera o mesmo índice, mas o índice não pode ser revertido pro email original).
 *
 * As duas chaves são independentes de propósito: a de cifragem só é necessária quando a
 * aplicação precisa ler o email de volta (ex: enviar um e-mail); a do índice só serve
 * pra comparar, nunca decifra nada.
 */
@Component
public class EmailCipherService {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec encryptionKey;
    private final SecretKeySpec indexKey;

    public EmailCipherService(
            @Value("${security.email-encryption.key}") String base64EncryptionKey,
            @Value("${security.email-encryption.index-secret}") String base64IndexSecret
    ) {
        this.encryptionKey = new SecretKeySpec(Base64.getDecoder().decode(base64EncryptionKey), "AES");
        this.indexKey = new SecretKeySpec(Base64.getDecoder().decode(base64IndexSecret), "HmacSHA256");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv).put(ciphertext);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao cifrar email.", e);
        }
    }

    public String decrypt(String encoded) {
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);

            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao decifrar email.", e);
        }
    }

    public String blindIndex(String email) {
        try {
            String normalized = email.trim().toLowerCase();

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(indexKey);
            byte[] hash = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao gerar índice de busca do email.", e);
        }
    }
}
