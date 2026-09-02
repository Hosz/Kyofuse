package com.hokyozu.kyofuse.infrastructure.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Registrado como bean Spring (em vez de instanciado via reflection pelo Hibernate) pra
 * poder receber o {@link TotpSecretCipherService} injetado — mesmo motivo do
 * {@link EncryptedEmailConverter}: {@code User.getTotpSecret()}/{@code setTotpSecret()}
 * continuam trabalhando com o secret em texto puro, e só o valor gravado/lido do banco
 * passa por aqui.
 */
@Converter
@Component
@RequiredArgsConstructor
public class EncryptedTotpSecretConverter implements AttributeConverter<String, String> {

    private final TotpSecretCipherService totpSecretCipherService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : totpSecretCipherService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : totpSecretCipherService.decrypt(dbData);
    }
}
