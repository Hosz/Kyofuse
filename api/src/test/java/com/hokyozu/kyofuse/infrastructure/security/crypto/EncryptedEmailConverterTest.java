package com.hokyozu.kyofuse.infrastructure.security.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptedEmailConverterTest {

    @Mock
    private EmailCipherService emailCipherService;

    @InjectMocks
    private EncryptedEmailConverter converter;

    @Test
    void convertToDatabaseColumnEncryptsNonNullValues() {
        when(emailCipherService.encrypt("hideo@example.com")).thenReturn("cipher-value");

        assertThat(converter.convertToDatabaseColumn("hideo@example.com")).isEqualTo("cipher-value");
    }

    @Test
    void convertToEntityAttributeDecryptsNonNullValues() {
        when(emailCipherService.decrypt("cipher-value")).thenReturn("hideo@example.com");

        assertThat(converter.convertToEntityAttribute("cipher-value")).isEqualTo("hideo@example.com");
    }

    @Test
    void nullValuesPassThroughWithoutTouchingTheCipher() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();

        verifyNoInteractions(emailCipherService);
    }
}
