package com.hokyozu.kyofuse.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class StartupSecurityValidator {

    private static final Set<String> KNOWN_WEAK_SECRETS = Set.of(
            "change-me",
            "changeme",
            "password",
            "admin",
            "123456",
            "12345678",
            "secret",
            "kyofuse_redis_secret",
            "kyofuse_storage_secret",
            "kyofuse_storage_user",
            "generate-a-long-random-secret",
            "generate-a-base64-256-bit-key",
            "generate-a-different-base64-256-bit-key",
            "generate-a-different-long-random-secret",
            "your-google-client-id.apps.googleusercontent.com",
            "your-steam-web-api-key"
    );

    private final Environment environment;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${storage.s3.secret-key:}")
    private String storageSecretKey;

    @Value("${security.jwt.secret:}")
    private String jwtSecret;

    @Value("${security.jwt.mfa-secret:}")
    private String mfaJwtSecret;

    @Value("${security.email-encryption.key:}")
    private String emailEncryptionKey;

    @Value("${security.email-encryption.index-secret:}")
    private String emailIndexSecret;

    @Value("${security.totp-encryption.key:}")
    private String totpEncryptionKey;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.startup-validation.enforce:#{null}}")
    private Boolean enforceExplicit;

    @Autowired
    public StartupSecurityValidator(Environment environment) {
        this.environment = environment;
    }

    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public void setMfaJwtSecret(String mfaJwtSecret) { this.mfaJwtSecret = mfaJwtSecret; }
    public void setEmailEncryptionKey(String emailEncryptionKey) { this.emailEncryptionKey = emailEncryptionKey; }
    public void setEmailIndexSecret(String emailIndexSecret) { this.emailIndexSecret = emailIndexSecret; }
    public void setTotpEncryptionKey(String totpEncryptionKey) { this.totpEncryptionKey = totpEncryptionKey; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }
    public void setRedisPassword(String redisPassword) { this.redisPassword = redisPassword; }
    public void setStorageSecretKey(String storageSecretKey) { this.storageSecretKey = storageSecretKey; }
    public void setCookieSecure(boolean cookieSecure) { this.cookieSecure = cookieSecure; }
    public void setEnforceExplicit(Boolean enforceExplicit) { this.enforceExplicit = enforceExplicit; }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        validateSecurityConfiguration();
    }

    public List<String> validateSecurityConfiguration() {
        List<String> violations = new ArrayList<>();

        boolean isProd = isProductionEnvironment();

        checkSecret("security.jwt.secret", jwtSecret, 32, violations);
        checkSecret("security.jwt.mfa-secret", mfaJwtSecret, 32, violations);
        checkSecret("spring.data.redis.password", redisPassword, 16, violations);
        checkSecret("storage.s3.secret-key", storageSecretKey, 16, violations);

        if (jwtSecret != null && !jwtSecret.isBlank() && jwtSecret.equals(mfaJwtSecret)) {
            violations.add("security.jwt.secret e security.jwt.mfa-secret não podem ser idênticos.");
        }

        checkSecret("security.email-encryption.key", emailEncryptionKey, 32, violations);
        checkSecret("security.email-encryption.index-secret", emailIndexSecret, 32, violations);

        if (emailEncryptionKey != null && !emailEncryptionKey.isBlank() && emailEncryptionKey.equals(emailIndexSecret)) {
            violations.add("security.email-encryption.key e security.email-encryption.index-secret não podem ser idênticos.");
        }

        checkSecret("security.totp-encryption.key", totpEncryptionKey, 32, violations);

        if (emailEncryptionKey != null && !emailEncryptionKey.isBlank() && emailEncryptionKey.equals(totpEncryptionKey)) {
            violations.add("security.email-encryption.key e security.totp-encryption.key não podem ser idênticos.");
        }

        checkSecret("spring.datasource.password", dbPassword, 8, violations);

        if (isProd && !cookieSecure) {
            violations.add("security.cookie.secure deve ser 'true' em ambiente de produção.");
        }

        if (!violations.isEmpty()) {
            if (isProd) {
                String errorReport = String.join("\n - ", violations);
                log.error("[STARTUP SECURITY ERROR] Bloqueando inicialização em produção devido a configurações inseguras:\n - {}", errorReport);
                throw new IllegalStateException("FALHA DE INICIALIZAÇÃO SEGURA: Segredos inseguros ou inválidos detectados em ambiente de produção:\n - " + errorReport);
            } else {
                for (String violation : violations) {
                    log.warn("[STARTUP SECURITY WARNING] Configuração insegura detectada (permitida apenas em dev/test): {}", violation);
                }
            }
        } else {
            log.info("[STARTUP SECURITY] Validação de segurança concluída com sucesso. Nenhum segredo fraco ou padrão detectado.");
        }

        return violations;
    }

    private void checkSecret(String propertyName, String value, int minLength, List<String> violations) {
        if (value == null || value.isBlank()) {
            violations.add(propertyName + " não pode estar vazio.");
            return;
        }

        String normalized = value.trim().toLowerCase();
        if (KNOWN_WEAK_SECRETS.contains(normalized)) {
            violations.add(propertyName + " está utilizando um valor padrão/conhecido inseguro ('" + value + "').");
            return;
        }

        if (value.trim().length() < minLength) {
            violations.add(propertyName + " possui entropia insuficiente (mínimo de " + minLength + " caracteres).");
        }
    }

    private boolean isProductionEnvironment() {
        if (enforceExplicit != null) {
            return enforceExplicit;
        }
        if (environment == null || environment.getActiveProfiles() == null) {
            return false;
        }
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return activeProfiles.contains("prod") || activeProfiles.contains("production");
    }
}
