package com.hokyozu.kyofuse.infrastructure.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Registrado como bean Spring (em vez de instanciado via reflection pelo Hibernate) pra
 * poder receber o {@link EmailCipherService} injetado — é isso que torna a cifragem
 * transparente: {@code User.getEmail()}/{@code setEmail()} continuam trabalhando com
 * texto puro, e só o valor gravado/lido do banco passa por aqui.
 */
@Converter
@Component
@RequiredArgsConstructor
public class EncryptedEmailConverter implements AttributeConverter<String, String> {

    private final EmailCipherService emailCipherService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : emailCipherService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : emailCipherService.decrypt(dbData);
    }
}
