package com.hokyozu.kyofuse.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartupSecurityValidatorTest {

    @Mock
    private Environment environment;

    private StartupSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StartupSecurityValidator(environment);
        validator.setJwtSecret("0123456789abcdef0123456789abcdef0123456789abcdef");
        validator.setMfaJwtSecret("abcdef0123456789abcdef0123456789abcdef0123456789");
        validator.setEmailEncryptionKey("dGhpcy1pcy1hLXZhbGlkLWJhc2U2NC1rZXktZm9yLWVudmlyb25tZW50IQ==");
        validator.setEmailIndexSecret("YW5vdGhlci12YWxpZC1iYXNlNjQta2V5LWZvci1pbmRleC1zZWNyZXQh");
        validator.setTotpEncryptionKey("eWV0LWFub3RoZXItdmFsaWQtYmFzZTY0LWtleS1mb3ItdG90cC1zZWNyZXQ=");
        validator.setDbPassword("AStrongDbPassword123!");
        validator.setRedisPassword("AStrongRedisPassword123!");
        validator.setStorageSecretKey("AStrongStorageSecretKey123!");
        validator.setCookieSecure(true);
    }

    @Test
    @DisplayName("Deve passar na validação quando todos os segredos forem fortes e seguros em produção")
    void validateSecurityConfiguration_whenValidInProduction_succeeds() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        List<String> violations = validator.validateSecurityConfiguration();

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve bloquear inicialização em produção quando segredo JWT for fraco ou conhecido")
    void validateSecurityConfiguration_whenWeakJwtSecretInProduction_throwsException() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        validator.setJwtSecret("change-me");

        assertThatThrownBy(() -> validator.validateSecurityConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("valor padrão/conhecido inseguro");
    }

    @Test
    @DisplayName("Deve bloquear inicialização em produção quando segredo MFA for idêntico ao JWT normal")
    void validateSecurityConfiguration_whenReusedJwtSecretInProduction_throwsException() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        String reused = "0123456789abcdef0123456789abcdef0123456789abcdef";
        validator.setJwtSecret(reused);
        validator.setMfaJwtSecret(reused);

        assertThatThrownBy(() -> validator.validateSecurityConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não podem ser idênticos");
    }

    @Test
    @DisplayName("Deve bloquear inicialização em produção quando cookie.secure for falso")
    void validateSecurityConfiguration_whenInsecureCookiesInProduction_throwsException() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        validator.setCookieSecure(false);

        assertThatThrownBy(() -> validator.validateSecurityConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("security.cookie.secure deve ser 'true'");
    }

    @Test
    @DisplayName("Deve bloquear inicialização em produção quando senha do Redis for fraca ou conhecida")
    void validateSecurityConfiguration_whenWeakRedisPasswordInProduction_throwsException() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        validator.setRedisPassword("kyofuse_redis_secret");

        assertThatThrownBy(() -> validator.validateSecurityConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.data.redis.password está utilizando um valor padrão/conhecido inseguro");
    }

    @Test
    @DisplayName("Deve bloquear inicialização em produção quando storage secret for fraco ou conhecido")
    void validateSecurityConfiguration_whenWeakStorageSecretInProduction_throwsException() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        validator.setStorageSecretKey("kyofuse_storage_secret");

        assertThatThrownBy(() -> validator.validateSecurityConfiguration())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("storage.s3.secret-key está utilizando um valor padrão/conhecido inseguro");
    }

    @Test
    @DisplayName("Não deve lançar exceção em perfil dev mesmo se houver segredos fracos, apenas coletar alertas")
    void validateSecurityConfiguration_whenWeakSecretsInDev_doesNotThrow() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        validator.setJwtSecret("change-me");
        validator.setCookieSecure(false);

        List<String> violations = validator.validateSecurityConfiguration();

        assertThat(violations).isNotEmpty();
    }
}
